package no.nav.syfo.sykmelding.api

import io.ktor.http.*
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import no.nav.syfo.HttpMessage
import no.nav.syfo.logger
import no.nav.syfo.sykmelding.DeleteSykmeldingException
import no.nav.syfo.sykmelding.DeleteSykmeldingService
import no.nav.syfo.utils.UnauthorizedException
import no.nav.syfo.utils.getAccessTokenFromAuthHeader

fun Route.registerDeleteSykmeldingApi(deleteSykmeldingService: DeleteSykmeldingService) {
    delete("/api/sykmelding/{sykmeldingId}/{journalpostId}") {
        val sykmeldingId = call.parameters["sykmeldingId"]!!
        val journalpostId = call.parameters["journalpostId"]!!
        logger.info("Deleting sykmelding $sykmeldingId")
        if (sykmeldingId.isEmpty()) {
            call.respond(HttpStatusCode.BadRequest, HttpMessage("Sykmeldingid må være satt"))
            return@delete
        }

        if (journalpostId.isEmpty()) {
            call.respond(HttpStatusCode.BadRequest, HttpMessage("journalpostId må være satt"))
            return@delete
        }

        logger.info("Deleting sykmelding $sykmeldingId and journalpostId $journalpostId")

        try {
            val accessToken = getAccessTokenFromAuthHeader(call.request)

            deleteSykmeldingService.deleteSykmelding(sykmeldingId, journalpostId, accessToken)
            logger.info(
                "Sender http OK status tilbake for sletting av sykmelding med id $sykmeldingId"
            )
            call.respond(HttpStatusCode.OK, HttpMessage("Vellykket sletting"))
        } catch (unauthorizedException: UnauthorizedException) {
            logger.warn("Fant ikkje authorization header: ", unauthorizedException)
            call.respond(HttpStatusCode.Unauthorized, HttpMessage("Unauthorized"))
        } catch (deleteSykmeldingException: DeleteSykmeldingException) {
            logger.warn("Fant ikkje sykmelding: ", deleteSykmeldingException)
            call.respond(
                HttpStatusCode.NotFound,
                HttpMessage("Fant ikkje sykmelding med sykmeldingid: $sykmeldingId")
            )
        } catch (e: Exception) {
            logger.error("Kastet exception ved sletting av sykmelding med id $sykmeldingId", e)
            call.respond(
                HttpStatusCode.InternalServerError,
                HttpMessage("Noe gikk galt ved sletting av sykmelding, prøv igjen")
            )
        }
    }
}
