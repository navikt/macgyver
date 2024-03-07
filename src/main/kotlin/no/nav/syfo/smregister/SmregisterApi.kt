package no.nav.syfo.smregister

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import no.nav.syfo.db.Database
import no.nav.syfo.db.toList
import no.nav.syfo.kafka.aiven.KafkaUtils.Companion.getAivenKafkaConfig
import no.nav.syfo.kafka.toProducerConfig
import no.nav.syfo.logger
import no.nav.syfo.utils.JacksonKafkaSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.time.LocalDateTime
import java.util.Properties
import kotlin.time.measureTimedValue
data class SykmeldingIdAndMottattDato(val sykmeldingId: String, val mottattDato: LocalDateTime)
fun Database.getSykmeldingIderAndMottattDatoForYear(year: Int): List<Pair<String, LocalDateTime>> {
    var count: Long = 0
    return connection.use {
        it.prepareStatement(
            """
            select id, mottatt_tidspunkt from sykmeldingsopplysninger where extract(YEAR from mottatt_tidspunkt) = ?;
        """,
        ).use { ps ->
            ps.setInt(1, year)
            ps.executeQuery().toList {
                count++
                if (count % 100_000 == 0L) {
                    logger.info("Got $count sykmeldinger_ids from smregister")
                }
                getString("id") to getTimestamp("mottatt_tidspunkt").toLocalDateTime()
            }
        }
    }
}
val kafkaProducer = KafkaProducer<String, SykmeldingIdAndMottattDato>(
    getAivenKafkaConfig("macgyver-migrations").toProducerConfig(
        "macgyver-migrations",
        JacksonKafkaSerializer::class,
    )
)
@OptIn(DelicateCoroutinesApi::class)
fun Route.setupSmregisterApi(database: Database) {
    var running = false
    var error = false
    post("/api/smregister/job") {
        val year = call.parameters.get("year")?.toIntOrNull()
        if (year == null) {
            call.respond(HttpStatusCode.BadRequest, mapOf("message" to "year is missing or not a number"))
            return@post
        }
        if (running) {
            call.respond(HttpStatusCode.Conflict, mapOf("message" to "Job is already running"))
            return@post
        }
        call.respond(HttpStatusCode.Accepted, mapOf("running" to true))
        running = true
        error = false
        GlobalScope.launch(Dispatchers.IO) {
            logger.info("Getting sykmeldinger_ids from smregister")
            try {
                val timedValue = measureTimedValue {
                    logger.info("Getting sykmeldinger_ids from smregister for year $year")
                    val result = database.getSykmeldingIderAndMottattDatoForYear(year).map {
                        SykmeldingIdAndMottattDato(it.first, it.second)
                    }
                    result.forEach {
                        kafkaProducer.send(ProducerRecord("tsm.regdump", it.sykmeldingId, it)) { metadata, exception ->
                            if (exception != null && !error) {
                                logger.error("Failed to send sykmelding_id to kafka", exception)
                                error = true
                            }
                        }
                    }
                    result
                }
                logger.info("Got ${timedValue.value.size} sykmeldinger_ids from smregister for year $year in ${timedValue.duration.inWholeSeconds} seconds")
            } catch (e: Exception) {
                logger.error("Failed to get sykmeldinger_ids from smregister", e)
            } finally {
                running = false
            }
        }
    }
}
