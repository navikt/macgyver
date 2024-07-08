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

fun Route.registrerNarmestelederApi() {
    val narmestelederService by inject<NarmestelederService>()

    post("/narmesteleder/request") {
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

    get("/narmesteleder") {
        val principal = call.safePrincipal()
        val fnr = call.request.headers["fnr"]

        if (fnr == null) {
            call.respond(HttpStatusCode.BadRequest, HttpMessage("Mangler fnr i header"))
            return@get
        }

        auditlogg.info(
            AuditLogger(principal.email)
                .createcCefMessage(
                    fnr = fnr,
                    operation = AuditLogger.Operation.READ,
                    requestPath = "/api/narmesteleder",
                    permit = AuditLogger.Permit.PERMIT,
                ),
        )

        val narmesteledereForSykmeldt = narmestelederService.getNarmesteLedereForSykmeldt(fnr)

        call.respond(HttpStatusCode.OK, narmesteledereForSykmeldt)
    }
}
