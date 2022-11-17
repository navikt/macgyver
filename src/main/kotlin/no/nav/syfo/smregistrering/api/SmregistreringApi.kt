package no.nav.syfo.smregistrering.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.syfo.application.HttpMessage
import no.nav.syfo.log
import no.nav.syfo.smregistrering.SmregistreringService
import no.nav.syfo.utils.getAccessTokenFromAuthHeader
import no.nav.syfo.utils.getNavIdentFromToken
import no.nav.syfo.utils.logNAVEpostAndActionToSecureLog

fun Route.registerFerdigstillRegistreringsoppgaveApi(smregistreringService: SmregistreringService) {
    post("/api/smregistrering/{journalpostId}/ferdigstill") {
        val journalpostId = call.parameters["journalpostId"]!!
        if (journalpostId.isEmpty() || journalpostId == "null") {
            call.respond(HttpStatusCode.BadRequest, HttpMessage("JournalpostId må være satt"))
        }
        val accessToken = getAccessTokenFromAuthHeader(call.request)
        val ferdigstiltAv = getNavIdentFromToken(accessToken)

        try {
            logNAVEpostAndActionToSecureLog(
                accessToken,
                "Ferdigstille smregistreringsoppgave for journalpostId $journalpostId"
            )
            smregistreringService.ferdigstillOppgave(journalpostId = journalpostId, ferdigstiltAv = ferdigstiltAv)
            call.respond(HttpStatusCode.OK, HttpMessage("Vellykket ferdigstilling."))
        } catch (e: Exception) {
            log.error("Kastet exception ved ferdigstilling av registreringsoppgave for journalpostId $journalpostId, ${e.message}")
            call.respond(HttpStatusCode.InternalServerError, HttpMessage("Noe gikk galt ved ferdigstilling av registreringsoppgave"))
        }
    }
}
