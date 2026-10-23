package no.nav.syfo.sykmelding.delete_sykmelding

import io.ktor.http.*
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import no.nav.syfo.logging.logger
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.utils.safePrincipal
import org.koin.ktor.ext.inject

fun Route.registerDeleteSykmeldingApi() {
    val deleteSykmeldingService by inject<DeleteSykmeldingService>()

    delete("/sykmelding/{sykmeldingId}/{journalpostId}") {
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
            deleteSykmeldingService.deleteSykmelding(
                sykmeldingId,
                journalpostId,
                call.safePrincipal()
            )
            logger.info(
                "Sender http OK status tilbake for sletting av sykmelding med id $sykmeldingId"
            )
            call.respond(HttpStatusCode.OK, HttpMessage("Vellykket sletting"))
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
