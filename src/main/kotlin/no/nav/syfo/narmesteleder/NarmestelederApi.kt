package no.nav.syfo.narmesteleder

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.logging.AuditLogger
import no.nav.syfo.logging.auditlogg
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.utils.safePrincipal
import org.koin.ktor.ext.inject

fun Route.registrerNarmestelederApi() {
    val narmestelederService by inject<NarmestelederService>()

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

        val narmesteledereForSykmeldt = narmestelederService.getNarmesteldereForSykmeldt(fnr)

        call.respond(HttpStatusCode.OK, narmesteledereForSykmeldt)
    }
    delete("/narmesteleder/{id}") {
        val principal = call.safePrincipal()
        val id = call.parameters["id"]
        val fnr = call.request.headers["fnr"]
        val orgnummer = call.request.headers["orgnummer"]
        if (orgnummer == null || fnr == null || id == null) {
            call.respond(HttpStatusCode.BadRequest, HttpMessage("Mangler fnr or orgnummer i header"))
            return@delete
        }
        val narmesteledereForSykmeldt = narmestelederService.getNarmesteldereForSykmeldt(fnr)
        val nl = narmesteledereForSykmeldt.single { it.narmesteLederId == id && it.orgnummer == orgnummer && it.fnr == fnr }
        auditlogg.info(
            AuditLogger(principal.email)
                .createcCefMessage(
                    fnr = fnr,
                    operation = AuditLogger.Operation.WRITE,
                    requestPath = "/api/narmesteleder/$id",
                    permit = AuditLogger.Permit.PERMIT,
                ),
        )
        if(nl.aktivTom == null) {
            narmestelederService.deaktiverNarmesteLeder(fnr, orgnummer)
        }
        call.respond(HttpStatusCode.OK, HttpMessage("Vellykket deaktivering"))
    }

    get("/narmesteleder/leder") {
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
                    requestPath = "/api/narmesteleder/leder",
                    permit = AuditLogger.Permit.PERMIT,
                ),
        )

        val narmesteledereForSykmeldt = narmestelederService.getNarmestelderKoblingerForLeder(fnr)

        call.respond(HttpStatusCode.OK, narmesteledereForSykmeldt)
    }
}
