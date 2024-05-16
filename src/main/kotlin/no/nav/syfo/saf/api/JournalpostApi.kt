package no.nav.syfo.saf.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.syfo.logging.AuditLogger
import no.nav.syfo.logging.auditlogg
import no.nav.syfo.logging.logger
import no.nav.syfo.logging.sikkerlogg
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.saf.service.SafService
import no.nav.syfo.utils.getAccessTokenFromAuthHeader
import org.koin.ktor.ext.inject

fun Route.registerJournalpostApi() {
    val safService by inject<SafService>()

    get("/api/journalposter/{fnr}") {
        val fnr = call.parameters["fnr"]
        sikkerlogg.info("prøver å hente journalposter på fnr $fnr")

        if (fnr.isNullOrEmpty()) {
            logger.warn("fnr kan ikke være null eller tom")
            call.respond(HttpStatusCode.BadRequest, HttpMessage("fnr kan ikke være null eller tom"))
            return@get
        }

        try {
            sikkerlogg.info("auditlogger")
            auditlogg.info(
                AuditLogger()
                    .createcCefMessage(
                        fnr = fnr,
                        accessToken = getAccessTokenFromAuthHeader(call.request),
                        operation = AuditLogger.Operation.WRITE,
                        requestPath = "/api/journalposter/$fnr",
                        permit = AuditLogger.Permit.PERMIT,
                    ),
            )
            sikkerlogg.info("Henter journalposter fra saf-api fnr: $fnr")

            val journalposter = safService.getDokumentoversiktBruker(fnr)

            if (journalposter == null) {
                logger.info(
                    "Sender http OK status tilbake, for henting av journalposter fra saf-api"
                )
                call.respond(HttpStatusCode.NotFound, "Fant ingen journalposter")
            } else {
                logger.info(
                    "Sender http OK status tilbake, for henting av journalposter fra saf-api"
                )
                call.respond(HttpStatusCode.OK, journalposter)
            }
        } catch (e: Exception) {
            logger.error("Kastet exception ved henting av journalposter fra saf-api", e)
            call.respond(
                HttpStatusCode.InternalServerError,
                HttpMessage("Noe gikk galt ved henting av journalposter")
            )
        }
    }
}
