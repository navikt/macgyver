package no.nav.syfo.oppgave.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import java.util.UUID
import no.nav.syfo.auditlogger.AuditLogger
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.oppgave.client.OppgaveClient
import no.nav.syfo.utils.auditlogg
import no.nav.syfo.utils.getAccessTokenFromAuthHeader
import no.nav.syfo.utils.logger
import no.nav.syfo.utils.sikkerlogg

fun Route.registerHentOppgaverApi(oppgaveClient: OppgaveClient) {
    post("/api/oppgave/list") {
        val callId = UUID.randomUUID().toString()

        val ids = call.receive<List<Int>>()

        if (ids.isEmpty()) {
            logger.warn("Listen med oppgaveId-er kan ikke være tom")
            call.respond(
                HttpStatusCode.BadRequest,
                HttpMessage("Listen med oppgaveId-er kan ikke være tom")
            )
            return@post
        }

        try {
            auditlogg.info(
                AuditLogger()
                    .createcCefMessage(
                        fnr = null,
                        accessToken = getAccessTokenFromAuthHeader(call.request),
                        operation = AuditLogger.Operation.WRITE,
                        requestPath = "/api/oppgave/list",
                        permit = AuditLogger.Permit.PERMIT,
                    ),
            )
            sikkerlogg.info("Henter oppgaver fra Oppgave-api ider: $ids")

            val toList =
                ids.map { oppgaveClient.hentOppgave(oppgaveId = it, msgId = callId) }.toList()
            logger.info(
                "Sender http OK status tilbake, for henting av oppgaver fra Oppgave-api ider: $ids"
            )
            call.respond(HttpStatusCode.OK, toList)
        } catch (e: Exception) {
            logger.error("Kastet exception ved henting av oppgaver fra oppgave-api", e)
            call.respond(
                HttpStatusCode.InternalServerError,
                HttpMessage("Noe gikk galt ved henting av oppgave")
            )
        }
    }
}
