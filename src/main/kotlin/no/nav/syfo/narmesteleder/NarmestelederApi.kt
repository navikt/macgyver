package no.nav.syfo.narmesteleder

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.logging.AuditLogger
import no.nav.syfo.logging.auditlogg
import no.nav.syfo.logging.sikkerlogg
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.utils.safePrincipal
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
        val principal = call.safePrincipal()
        val nlRequest = call.receive<NarmestelederAltinnRequestPayload>()

        auditlogg.info(
            AuditLogger(principal.email)
                .createcCefMessage(
                    fnr = nlRequest.fnr,
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
        val principal = call.safePrincipal()
        val narmestelderRequest = call.receive<NarmestelederForSykmeldtPayload>()

        auditlogg.info(
            AuditLogger(principal.email)
                .createcCefMessage(
                    fnr = narmestelderRequest.sykmeldtFnr,
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
