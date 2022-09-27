package no.nav.syfo.papirsykmelding.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.syfo.application.HttpMessage
import no.nav.syfo.sykmelding.model.Periode

fun Route.registrerPeriodeApi(updatePeriodeService: UpdatePeriodeService) {
    post("/api/papirsykmelding/{sykmeldingId}/periode") {
        val sykmeldingId = call.parameters["sykmeldingId"]!!
        if (sykmeldingId.isEmpty()) {
            call.respond(HttpStatusCode.BadRequest, HttpMessage("Sykmeldingid må være satt"))
        }

        val periodeListeDTO = call.receive<PeriodeListeDTO>()
        if (periodeListeDTO.periodeliste.isEmpty()) {
            call.respond(HttpStatusCode.BadRequest, HttpMessage("Periodelisten kan ikke være tom"))
        }
        periodeListeDTO.periodeliste.forEach {
            if (it.tom.isBefore(it.fom)) {
                call.respond(HttpStatusCode.BadRequest, HttpMessage("FOM må være før TOM"))
            }
        }

        updatePeriodeService.updatePeriode(sykmeldingId = sykmeldingId, periodeliste = periodeListeDTO.periodeliste)
        call.respond(HttpStatusCode.OK, HttpMessage("Vellykket oppdatering."))
    }
}

data class PeriodeListeDTO(
    val periodeliste: List<Periode>
)
