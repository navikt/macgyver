package no.nav.syfo.smregistrering.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.syfo.HttpMessage
import no.nav.syfo.logger
import no.nav.syfo.smregistrering.SmregistreringService
import no.nav.syfo.utils.getAccessTokenFromAuthHeader
import no.nav.syfo.utils.logNAVEpostAndActionToSecureLog

fun Route.registerFerdigstillRegistreringsoppgaveApi(smregistreringService: SmregistreringService) {
    post("/api/smregistrering/{journalpostId}/ferdigstill") {
        val journalpostId = call.parameters["journalpostId"]!!
        if (journalpostId.isEmpty() || journalpostId == "null") {
            call.respond(HttpStatusCode.BadRequest, HttpMessage("JournalpostId må være satt"))
            return@post
        }
        val ferdigstiltAv = call.receive<FerdigstillSmregOppgave>().ferdigstiltAv
        if (ferdigstiltAv.length != 7) {
            call.respond(
                HttpStatusCode.BadRequest,
                HttpMessage("FerdigstiltAv må være en ident som består av 7 tegn")
            )
            return@post
        }

        try {
            logNAVEpostAndActionToSecureLog(
                getAccessTokenFromAuthHeader(call.request),
                "Ferdigstille smregistreringsoppgave for journalpostId $journalpostId",
            )
            smregistreringService.ferdigstillOppgave(
                journalpostId = journalpostId,
                ferdigstiltAv = ferdigstiltAv
            )
            call.respond(HttpStatusCode.OK, HttpMessage("Vellykket ferdigstilling."))
        } catch (e: Exception) {
            logger.error(
                "Kastet exception ved ferdigstilling av registreringsoppgave for journalpostId $journalpostId, ${e.message}"
            )
            call.respond(
                HttpStatusCode.InternalServerError,
                HttpMessage("Noe gikk galt ved ferdigstilling av registreringsoppgave")
            )
        }
    }
}
