package no.nav.syfo.narmesteleder.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import no.nav.syfo.auditlogger.AuditLogger
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.narmesteleder.NarmestelederService
import no.nav.syfo.utils.auditlogg
import no.nav.syfo.utils.getAccessTokenFromAuthHeader
import no.nav.syfo.utils.sikkerlogg

fun Route.registrerNarmestelederApi(narmestelederService: NarmestelederService) {
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

    get("/api/narmesteleder") {
        val narmestelderRequest = call.receive<NarmesteldereRequestDTO>()

        auditlogg.info(
            AuditLogger()
                .createcCefMessage(
                    fnr = narmestelderRequest.sykmeldtFnr,
                    accessToken = getAccessTokenFromAuthHeader(call.request),
                    operation = AuditLogger.Operation.READ,
                    requestPath = "/api/narmesteleder",
                    permit = AuditLogger.Permit.PERMIT,
                ),
        )

        val narmesteldereForSykmeldt =
            narmestelederService.getNarmesteldereForSykmeldt(narmestelderRequest.sykmeldtFnr)
        call.respond(HttpStatusCode.OK, narmesteldereForSykmeldt)
    }
}
