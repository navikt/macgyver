package no.nav.syfo.papirsykmelding.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import java.time.LocalDate
import no.nav.syfo.utils.getAccessTokenFromAuthHeader
import no.nav.syfo.utils.logNAVEpostAndActionToSecureLog

fun Route.registrerBehandletDatoApi(updateBehandletDatoService: UpdateBehandletDatoService) {
    post("/api/papirsykmelding/{sykmeldingId}/behandletdato") {
        val sykmeldingId = call.parameters["sykmeldingId"]!!
        if (sykmeldingId.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Sykmeldingid må være satt")
        }

        logNAVEpostAndActionToSecureLog(
            getAccessTokenFromAuthHeader(call.request),
            "Endre behandletdato ein papir sykmelding med id $sykmeldingId"
        )

        val behandletDatoDTO = call.receive<BehandletDatoDTO>()

        updateBehandletDatoService.updateBehandletDato(sykmeldingId = sykmeldingId, behandletDato = behandletDatoDTO.behandletDato)
        call.respond(HttpStatusCode.OK, "Vellykket oppdatering.")
    }
}

data class BehandletDatoDTO(
    val behandletDato: LocalDate
)
