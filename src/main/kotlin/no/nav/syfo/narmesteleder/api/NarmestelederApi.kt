package no.nav.syfo.narmesteleder.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.syfo.HttpMessage
import no.nav.syfo.narmesteleder.NarmestelederService
import no.nav.syfo.utils.getAccessTokenFromAuthHeader
import no.nav.syfo.utils.logNAVEpostAndActionToSecureLog

fun Route.registrerNarmestelederRequestApi(narmestelederService: NarmestelederService) {
    post("/api/narmesteleder/request") {
        val nlRequest = call.receive<NlRequestDTO>()
        logNAVEpostAndActionToSecureLog(
            getAccessTokenFromAuthHeader(call.request),
            "Sender ny NL-request til altinn for sykmelding med id: ${nlRequest.sykmeldingId}",
        )
        narmestelederService.sendNewNlRequest(nlRequest)
        call.respond(HttpStatusCode.OK, HttpMessage("Vellykket oppdatering."))
    }
}
