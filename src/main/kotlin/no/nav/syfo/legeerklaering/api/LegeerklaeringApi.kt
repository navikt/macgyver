package no.nav.syfo.legeerklaering.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import no.nav.syfo.HttpMessage
import no.nav.syfo.auditlogger.AuditLogger
import no.nav.syfo.legeerklaering.service.DeleteLegeerklaeringService
import no.nav.syfo.utils.UnauthorizedException
import no.nav.syfo.utils.auditlogg
import no.nav.syfo.utils.getAccessTokenFromAuthHeader
import no.nav.syfo.utils.logger

fun Route.registerDeleteLegeerklaeringApi(
    deleteLegeerklaeringService: DeleteLegeerklaeringService
) {
    delete("/api/legeerklaering/{legeerklaeringId}") {
        val legeerklaeringId = call.parameters["legeerklaeringId"]!!

        try {
            auditlogg.info(
                AuditLogger()
                    .createcCefMessage(
                        fnr = null,
                        accessToken = getAccessTokenFromAuthHeader(call.request),
                        operation = AuditLogger.Operation.WRITE,
                        requestPath = "/api/legeerklaering/$legeerklaeringId",
                        permit = AuditLogger.Permit.PERMIT,
                    ),
            )

            deleteLegeerklaeringService.deleteLegeerklaering(legeerklaeringId)
            logger.info(
                "Sender http OK status tilbake for sletting av legeerklaering med id $legeerklaeringId"
            )
            call.respond(HttpStatusCode.OK, HttpMessage("Vellykket sletting"))
        } catch (unauthorizedException: UnauthorizedException) {
            logger.warn("Fant ikkje authorization header: ", unauthorizedException)
            call.respond(HttpStatusCode.Unauthorized, HttpMessage("Unauthorized"))
        } catch (e: Exception) {
            logger.error(
                "Kastet exception ved sletting av legeerklaering med id $legeerklaeringId",
                e
            )
            call.respond(
                HttpStatusCode.InternalServerError,
                HttpMessage("Noe gikk galt ved sletting av legeerklaering, prøv igjen"),
            )
        }
    }
}
