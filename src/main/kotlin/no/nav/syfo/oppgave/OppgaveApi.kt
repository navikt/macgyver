package no.nav.syfo.oppgave

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*
import no.nav.syfo.logging.AuditLogger
import no.nav.syfo.logging.auditlogg
import no.nav.syfo.logging.logger
import no.nav.syfo.logging.sikkerlogg
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.utils.safePrincipal
import org.koin.ktor.ext.inject

fun Route.registerHentOppgaverApi() {
    val oppgaveClient by inject<OppgaveClient>()

    post("/api/oppgave/list") {
        val callId = UUID.randomUUID().toString()

        val ids = call.receive<List<Int>>()

        if (ids.isEmpty()) {
            logger.warn("Listen med oppgaveId-er kan ikke være tom")
            call.respond(
                HttpStatusCode.BadRequest,
                HttpMessage("Listen med oppgaveId-er kan ikke være tom"),
            )
            return@post
        }

        try {
            val principal = call.safePrincipal()
            auditlogg.info(
                AuditLogger(principal.email)
                    .createcCefMessage(
                        fnr = null,
                        operation = AuditLogger.Operation.WRITE,
                        requestPath = "/api/oppgave/list",
                        permit = AuditLogger.Permit.PERMIT,
                    ),
            )
            sikkerlogg.info("Henter oppgaver fra Oppgave-api ider: $ids")

            val toList =
                ids.map { oppgaveClient.hentOppgave(oppgaveId = it, msgId = callId) }.toList()
            logger.info(
                "Sender http OK status tilbake, for henting av oppgaver fra Oppgave-api ider: $ids",
            )
            call.respond(HttpStatusCode.OK, toList)
        } catch (e: Exception) {
            logger.error("Kastet exception ved henting av oppgaver fra oppgave-api", e)
            call.respond(
                HttpStatusCode.InternalServerError,
                HttpMessage("Noe gikk galt ved henting av oppgave"),
            )
        }
    }
}
