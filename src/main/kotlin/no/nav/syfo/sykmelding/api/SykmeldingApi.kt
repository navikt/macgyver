package no.nav.syfo.sykmelding.api

import io.ktor.http.*
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.syfo.HttpMessage
import no.nav.syfo.logger
import no.nav.syfo.service.GjenapneSykmeldingService
import no.nav.syfo.utils.getAccessTokenFromAuthHeader
import no.nav.syfo.utils.logNAVEpostAndActionToSecureLog

fun Route.registerGjenapneSykmeldingApi(gjenapneSykmeldingService: GjenapneSykmeldingService) {
    post("/api/sykmelding/{sykmeldingId}/gjenapne") {
        val sykmeldingId = call.parameters["sykmeldingId"]!!
        if (sykmeldingId.isEmpty() || sykmeldingId == "null") {
            call.respond(HttpStatusCode.BadRequest, HttpMessage("Sykmeldingid må være satt"))
            return@post
        }

        try {
            logNAVEpostAndActionToSecureLog(
                getAccessTokenFromAuthHeader(call.request),
                "gjenåpne sykmelding med id $sykmeldingId",
            )
            gjenapneSykmeldingService.gjenapneSykmelding(sykmeldingId)
            call.respond(HttpStatusCode.OK, HttpMessage("Vellykket oppdatering."))
        } catch (e: Exception) {
            logger.error(
                "Kastet exception ved gjenåpning av sykmelding med id $sykmeldingId, ${e.message}"
            )
            call.respond(
                HttpStatusCode.InternalServerError,
                HttpMessage("Noe gikk galt ved gjenåpning av sykmelding")
            )
        }
    }
}
