package no.nav.syfo.narmesteleder

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import no.nav.syfo.logging.AuditLogger
import no.nav.syfo.logging.auditlogg
import no.nav.syfo.logging.sikkerlogg
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.utils.getAccessTokenFromAuthHeader
import org.koin.ktor.ext.inject

data class NarmestelederAltinnRequestPayload(
    val fnr: String,
    val orgnummer: String,
    val sykmeldingId: String,
)

data class NarmestelederForSykmeldtPayload(val sykmeldtFnr: String)

fun Route.registrerNarmestelederApi() {
    val narmestelederService by inject<NarmestelederService>()

    post("/api/narmesteleder/request") {
        val nlRequest = call.receive<NarmestelederAltinnRequestPayload>()

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
            "Sender ny NL-request til altinn for sykmelding med id: ${nlRequest.sykmeldingId}",
        )

        narmestelederService.sendNewNlRequest(nlRequest)
        call.respond(HttpStatusCode.OK, HttpMessage("Vellykket oppdatering."))
    }

    get("/api/narmesteleder") {
        val narmestelderRequest = call.receive<NarmestelederForSykmeldtPayload>()

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
