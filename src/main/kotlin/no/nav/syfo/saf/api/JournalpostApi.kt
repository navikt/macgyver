package no.nav.syfo.saf.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.syfo.application.HttpMessage
import no.nav.syfo.log
import no.nav.syfo.saf.service.SafService
import no.nav.syfo.utils.getAccessTokenFromAuthHeader
import no.nav.syfo.utils.logNAVEpostAndActionToSecureLog

fun Route.registerJournalpostApi(safService: SafService) {
    get("/api/journalposter/{fnr}") {
        val fnr = call.parameters["fnr"]

        if (fnr.isNullOrEmpty()) {
            log.warn("fnr kan ikke være null eller tom")
            call.respond(HttpStatusCode.BadRequest, HttpMessage("fnr kan ikke være null eller tom"))
            return@get
        }

        try {
            logNAVEpostAndActionToSecureLog(
                getAccessTokenFromAuthHeader(call.request),
                "Henter journalposter fra saf-api fnr: $fnr",
            )
            val journalposter = safService.getDokumentoversiktBruker(fnr)

            if (journalposter == null) {
                log.info("Sender http OK status tilbake, for henting av journalposter fra saf-api")
                call.respond(HttpStatusCode.NotFound, "Fant ingen journalposter")
            } else {
                log.info("Sender http OK status tilbake, for henting av journalposter fra saf-api")
                call.respond(HttpStatusCode.OK, journalposter)
            }
        } catch (e: Exception) {
            log.error("Kastet exception ved henting av journalposter fra saf-api", e)
            call.respond(
                HttpStatusCode.InternalServerError,
                HttpMessage("Noe gikk galt ved henting av journalposter")
            )
        }
    }
}
