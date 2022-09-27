package no.nav.syfo.sykmelding.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import no.nav.syfo.log
import no.nav.syfo.sykmelding.DeleteSykmeldingException
import no.nav.syfo.sykmelding.DeleteSykmeldingService
import no.nav.syfo.utils.UnauthorizedException
import no.nav.syfo.utils.getAccessTokenFromAuthHeader
import no.nav.syfo.utils.logNAVEpostAndActionToSecureLog

fun Route.registerDeleteSykmeldingApi(deleteSykmeldingService: DeleteSykmeldingService) {
    delete("/api/sykmelding/{sykmeldingId}") {
        val sykmeldingId = call.parameters["sykmeldingId"]!!

        if (sykmeldingId.isEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Sykmeldingid må være satt")
        }

        try {
            logNAVEpostAndActionToSecureLog(
                getAccessTokenFromAuthHeader(call.request),
                "slette sykmelding med id $sykmeldingId"
            )

            deleteSykmeldingService.deleteSykmelding(sykmeldingId)
            log.info("Sender http OK status tilbake for sletting av sykmelding med id $sykmeldingId")
            call.respond(HttpStatusCode.OK, "Vellykket sletting")

        } catch (unauthorizedException: UnauthorizedException) {
            log.warn("Fant ikkje authorization header: ", unauthorizedException)
            call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
        } catch (deleteSykmeldingException: DeleteSykmeldingException) {
            log.warn("Fant ikkje sykmelding: ", deleteSykmeldingException)
            call.respond(HttpStatusCode.NotFound, "Fant ikkje sykmelding med sykmeldingid: $sykmeldingId")
        } catch (e: Exception) {
            log.error("Kastet exception ved sletting av sykmelding med id $sykmeldingId", e)
            call.respond(HttpStatusCode.InternalServerError, "Noe gikk galt ved sletting av sykmelding, prøv igjen")
        }
    }
}
