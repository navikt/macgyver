package no.nav.syfo.narmesteleder.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.syfo.HttpMessage
import no.nav.syfo.auditlogg
import no.nav.syfo.auditlogger.AuditLogger
import no.nav.syfo.narmesteleder.NarmestelederService
import no.nav.syfo.sikkerlogg
import no.nav.syfo.utils.getAccessTokenFromAuthHeader

fun Route.registrerNarmestelederRequestApi(narmestelederService: NarmestelederService) {
    post("/api/narmesteleder/request") {
        val nlRequest = call.receive<NlRequestDTO>()

        auditlogg.info(
            AuditLogger()
                .createcCefMessage(
                    fnr = nlRequest.fnr,
                    accessToken = getAccessTokenFromAuthHeader(call.request),
                    operation = AuditLogger.Operation.WRITE,
                    requestPath = "/api/narmesteleder/request",
                    permit = AuditLogger.Permit.PERMIT,
                ),
        )
        sikkerlogg.info(
            "Sender ny NL-request til altinn for sykmelding med id: ${nlRequest.sykmeldingId}"
        )

        narmestelederService.sendNewNlRequest(nlRequest)
        call.respond(HttpStatusCode.OK, HttpMessage("Vellykket oppdatering."))
    }
}
