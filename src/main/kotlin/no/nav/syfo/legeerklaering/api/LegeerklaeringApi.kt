package no.nav.syfo.legeerklaering.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import no.nav.syfo.application.HttpMessage
import no.nav.syfo.legeerklaering.service.DeleteLegeerklaeringService
import no.nav.syfo.log
import no.nav.syfo.utils.UnauthorizedException
import no.nav.syfo.utils.getAccessTokenFromAuthHeader
import no.nav.syfo.utils.logNAVEpostAndActionToSecureLog

fun Route.registerDeleteLegeerklaeringApi(deleteLegeerklaeringService: DeleteLegeerklaeringService) {
    delete("/api/legeerklaering/{legeerklaeringId}") {
        val legeerklaeringId = call.parameters["legeerklaeringId"]!!

        try {
            logNAVEpostAndActionToSecureLog(
                getAccessTokenFromAuthHeader(call.request),
                "slette legeerklaering med id $legeerklaeringId"
            )

            deleteLegeerklaeringService.deleteLegeerklaering(legeerklaeringId)
            log.info("Sender http OK status tilbake for sletting av legeerklaering med id $legeerklaeringId")
            call.respond(HttpStatusCode.OK, HttpMessage("Vellykket sletting"))
        } catch (unauthorizedException: UnauthorizedException) {
            log.warn("Fant ikkje authorization header: ", unauthorizedException)
            call.respond(HttpStatusCode.Unauthorized, HttpMessage("Unauthorized"))
        } catch (e: Exception) {
            log.error("Kastet exception ved sletting av legeerklaering med id $legeerklaeringId", e)
            call.respond(
                HttpStatusCode.InternalServerError,
                HttpMessage("Noe gikk galt ved sletting av legeerklaering, prøv igjen")
            )
        }
    }
}
