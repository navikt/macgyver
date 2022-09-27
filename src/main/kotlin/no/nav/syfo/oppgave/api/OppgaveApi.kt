package no.nav.syfo.oppgave.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.syfo.application.HttpMessage
import no.nav.syfo.log
import no.nav.syfo.oppgave.client.OppgaveClient
import no.nav.syfo.utils.getAccessTokenFromAuthHeader
import no.nav.syfo.utils.logNAVEpostAndActionToSecureLog
import java.util.UUID

fun Route.registerHentOppgaverApi(oppgaveClient: OppgaveClient) {
    post("/api/oppgave/list") {

        val callId = UUID.randomUUID().toString()

        val ids = call.receive<List<Int>>()

        if (ids.isEmpty()) {
            log.warn("Listen med oppgaveId-er kan ikke være tom")
            call.respond(HttpStatusCode.BadRequest, HttpMessage("Listen med oppgaveId-er kan ikke være tom"))
        }

        try {
            logNAVEpostAndActionToSecureLog(
                getAccessTokenFromAuthHeader(call.request),
                "Henter oppgaver fra Oppgave-api ider: $ids"
            )
            val toList = ids.map {
                oppgaveClient.hentOppgave(oppgaveId = it, msgId = callId)
            }.toList()
            log.info("Sender http OK status tilbake, for henting av oppgaver fra Oppgave-api ider: $ids")
            call.respond(HttpStatusCode.OK, toList)
        } catch (e: Exception) {
            log.error("Kastet exception ved henting av oppgaver fra oppgave-api", e)
            call.respond(HttpStatusCode.InternalServerError, HttpMessage("Noe gikk galt ved henting av oppgave"))
        }
    }
}
