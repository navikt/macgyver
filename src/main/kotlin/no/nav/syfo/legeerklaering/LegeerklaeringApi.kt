package no.nav.syfo.legeerklaering

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import no.nav.syfo.logging.AuditLogger
import no.nav.syfo.logging.auditlogg
import no.nav.syfo.logging.logger
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.utils.UnauthorizedException
import no.nav.syfo.utils.getAccessTokenFromAuthHeader
import org.koin.ktor.ext.inject

fun Route.registerDeleteLegeerklaeringApi() {
    val deleteLegeerklaeringService by inject<DeleteLegeerklaeringService>()

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
                "Sender http OK status tilbake for sletting av legeerklaering med id $legeerklaeringId",
            )
            call.respond(HttpStatusCode.OK, HttpMessage("Vellykket sletting"))
        } catch (unauthorizedException: UnauthorizedException) {
            logger.warn("Fant ikkje authorization header: ", unauthorizedException)
            call.respond(HttpStatusCode.Unauthorized, HttpMessage("Unauthorized"))
        } catch (e: Exception) {
            logger.error(
                "Kastet exception ved sletting av legeerklaering med id $legeerklaeringId",
                e,
            )
            call.respond(
                HttpStatusCode.InternalServerError,
                HttpMessage("Noe gikk galt ved sletting av legeerklaering, prøv igjen"),
            )
        }
    }
}
