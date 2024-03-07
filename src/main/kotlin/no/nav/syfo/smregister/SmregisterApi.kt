package no.nav.syfo.smregister

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import jnr.a64asm.Offset
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import no.nav.syfo.db.Database
import no.nav.syfo.db.toList
import no.nav.syfo.logger
import java.time.LocalDateTime
import java.time.OffsetDateTime
import kotlin.time.measureTimedValue

fun Database.getSykmeldingIderAndMottattDato(): List<Pair<String, LocalDateTime>> {
    var count: Long = 0
    return connection.use {
        it.prepareStatement(
            """
            select id, mottatt_tidspunkt from sykmeldingsopplysninger
        """,
        ).use { ps ->
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

fun Database.getSykmeldingIderAndMottattDatoForYear(year: Int): List<Pair<String, LocalDateTime>> {
    var count: Long = 0
    return connection.use {
        it.prepareStatement(
            """
            select count('any') from sykmeldingsopplysninger where extract(YEAR from mottatt_tidspunkt) = ?;
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

@OptIn(DelicateCoroutinesApi::class)
fun Route.setupSmregisterApi(database: Database) {
    var running = false
    post("/api/smregister/job") {
        val year = call.parameters.get("year")
        if(running) {
            call.respond(HttpStatusCode.Conflict, mapOf("message" to "Job is already running"))
            return@post
        }
        call.respond(HttpStatusCode.Accepted, mapOf("running" to true))
        running = true
        GlobalScope.launch(Dispatchers.IO) {
            logger.info("Getting sykmeldinger_ids from smregister")
            try {
                val timedValue = measureTimedValue {
                    val yearNumber = year?.toIntOrNull()
                    if(yearNumber != null) {
                        logger.info("Getting sykmeldinger_ids from smregister for year $year")
                         database.getSykmeldingIderAndMottattDatoForYear(yearNumber)
                    }
                    database.getSykmeldingIderAndMottattDato()
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
