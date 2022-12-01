package no.nav.syfo.papirsykmelding.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.syfo.application.HttpMessage
import no.nav.syfo.log
import no.nav.syfo.utils.getAccessTokenFromAuthHeader
import no.nav.syfo.utils.logNAVEpostAndActionToSecureLog
import java.time.LocalDate

fun Route.registrerBehandletDatoApi(updateBehandletDatoService: UpdateBehandletDatoService) {
    post("/api/papirsykmelding/{sykmeldingId}/behandletdato") {
        val sykmeldingId = call.parameters["sykmeldingId"]!!
        if (sykmeldingId.isEmpty()) {
            call.respond(HttpStatusCode.BadRequest, HttpMessage("Sykmeldingid må være satt"))
            return@post
        }

        try {
            logNAVEpostAndActionToSecureLog(
                getAccessTokenFromAuthHeader(call.request),
                "endre behandletdato ein papir sykmelding med id $sykmeldingId"
            )

            val behandletDatoDTO = call.receive<BehandletDatoDTO>()

            updateBehandletDatoService.updateBehandletDato(
                sykmeldingId = sykmeldingId,
                behandletDato = behandletDatoDTO.behandletDato
            )

            call.respond(HttpStatusCode.OK, HttpMessage("Vellykket oppdatering"))
        } catch (e: Exception) {
            log.error("Kastet exception ved endre behandletdato ein papir sykmelding med id $sykmeldingId", e)
            call.respond(
                HttpStatusCode.InternalServerError,
                HttpMessage("Noe gikk galt ved endre behandletdato på ein papir sykmelding, prøv igjen")
            )
        }
    }
}

data class BehandletDatoDTO(
    val behandletDato: LocalDate
)
