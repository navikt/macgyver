package no.nav.syfo.smregister

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
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
    return connection.use {
        it.prepareStatement(
            """
            select id, mottatt_tidspunkt from sykmeldingsopplysninger
        """,
        ).use { ps ->
            ps.executeQuery().toList {
                getString("id") to getTimestamp("mottatt_tidspunkt").toLocalDateTime()
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun Route.setupSmregisterApi(database: Database) {
    var running = false
    post("/api/smregister/job") {
        if(running) {
            call.respond(HttpStatusCode.Conflict, "Job is already running")
            return@post
        }
        call.respond(HttpStatusCode.Accepted)
        running = true
        GlobalScope.launch(Dispatchers.IO) {
            logger.info("Getting sykmeldinger_ids from smregister")
            try {
                val timedValue = measureTimedValue {
                    database.getSykmeldingIderAndMottattDato()
                }
                logger.info("Got ${timedValue.value.size} sykmeldinger_ids from smregister in ${timedValue.duration.inWholeSeconds} seconds")
                call.respond(HttpStatusCode.Created)
            } catch (e: Exception) {
                logger.error("Failed to get sykmeldinger_ids from smregister", e)
            } finally {
                running = false
            }
        }
    }
}
