package no.nav.syfo.papirsykmelding.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.syfo.application.HttpMessage
import no.nav.syfo.log
import no.nav.syfo.sykmelding.model.Periode
import no.nav.syfo.utils.getAccessTokenFromAuthHeader
import no.nav.syfo.utils.logNAVEpostAndActionToSecureLog

fun Route.registrerPeriodeApi(updatePeriodeService: UpdatePeriodeService) {
    post("/api/papirsykmelding/{sykmeldingId}/periode") {
        val sykmeldingId = call.parameters["sykmeldingId"]!!
        if (sykmeldingId.isEmpty()) {
            call.respond(HttpStatusCode.BadRequest, HttpMessage("Sykmeldingid må være satt"))
            return@post
        }
        log.info("Mottatt kall for å oppdatere periode for sykmelding med id $sykmeldingId")

        logNAVEpostAndActionToSecureLog(
            getAccessTokenFromAuthHeader(call.request),
            "endre perioder for sykmelding med id $sykmeldingId"
        )

        val periodeListeDTO = call.receive<PeriodeListeDTO>()
        if (periodeListeDTO.periodeliste.isEmpty()) {
            call.respond(HttpStatusCode.BadRequest, HttpMessage("Periodelisten kan ikke være tom"))
            return@post
        }
        periodeListeDTO.periodeliste.forEach {
            if (it.tom.isBefore(it.fom)) {
                call.respond(HttpStatusCode.BadRequest, HttpMessage("FOM må være før TOM"))
                return@post
            }
        }

        updatePeriodeService.updatePeriode(sykmeldingId = sykmeldingId, periodeliste = periodeListeDTO.periodeliste)
        call.respond(HttpStatusCode.OK, HttpMessage("Vellykket oppdatering."))
    }
}

data class PeriodeListeDTO(
    val periodeliste: List<Periode>
)
