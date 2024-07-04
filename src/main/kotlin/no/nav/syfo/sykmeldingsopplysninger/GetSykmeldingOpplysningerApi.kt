package no.nav.syfo.sykmeldingsopplysninger

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.logging.AuditLogger
import no.nav.syfo.logging.auditlogg
import no.nav.syfo.logging.logger
import no.nav.syfo.logging.sikkerlogg
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.utils.safePrincipal
import org.koin.ktor.ext.inject

fun Route.registerSykmeldingsOpplysningerApi() {
    val getSykmeldingOpplysningerService by inject<GetSykmeldingOpplysningerService>()

    get("/sykmeldingsopplysninger") {
        logger.info("Henter sykmeldingsopplysninger")
        val fnr = call.request.headers["fnr"]
        sikkerlogg.info("prøver å hente sykmeldingsopplysninger på fnr $fnr")

        if (fnr.isNullOrEmpty()) {
            logger.warn("fnr kan ikke være null eller tom")
            call.respond(HttpStatusCode.BadRequest, HttpMessage("fnr kan ikke være null eller tom"))
            return@get
        }

        //   val sykmeldingsOpplysninger =
        // sykmeldingsOpplysningerClient.getSykmeldingsopplysninger(fnr)
        val sykmeldingsOpplysninger =
            getSykmeldingOpplysningerService.getSykmeldingOpplysninger(fnr)
        val principal = call.safePrincipal()
        auditlogg.info(
            AuditLogger(principal.email)
                .createcCefMessage(
                    fnr = fnr,
                    operation = AuditLogger.Operation.READ,
                    requestPath = "/api/sykmeldingsopplysninger",
                    permit = AuditLogger.Permit.PERMIT,
                ),
        )
        call.respond(HttpStatusCode.OK, sykmeldingsOpplysninger)
    }
}
