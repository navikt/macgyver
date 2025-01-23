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
import no.nav.syfo.saf.error.JournalposterNotFoundException
import no.nav.syfo.saf.service.SafService
import no.nav.syfo.utils.safePrincipal
import org.koin.ktor.ext.inject

fun Route.registerSykmeldingsOpplysningerApi() {
    val getSykmeldingOpplysningerService by inject<GetSykmeldingOpplysningerService>()
    val safService by inject<SafService>()

    get("/sykmeldingsopplysninger") {
        logger.info("Henter sykmeldingsopplysninger")
        val fnr = call.request.headers["fnr"]
        sikkerlogg.info("prøver å hente sykmeldingsopplysninger på fnr $fnr")

        if (fnr.isNullOrEmpty()) {
            logger.warn("fnr kan ikke være null eller tom")
            call.respond(HttpStatusCode.BadRequest, HttpMessage("fnr kan ikke være null eller tom"))
            return@get
        }
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
        val sykmeldingsOpplysninger =
            getSykmeldingOpplysningerService.getSykmeldingOpplysninger(fnr)

        try {
            val journalposter = safService.getJournalPostsBruker(fnr)
            val oppdatertSykmeldinger =
                sykmeldingsOpplysninger.sykmeldinger.map { sykmelding ->
                    val sykmeldingPeriode =
                        sykmelding.perioder?.firstOrNull() ?: return@map sykmelding
                    val journalpostId =
                        journalposter
                            ?.find {
                                it.periode?.fom == sykmeldingPeriode.fom &&
                                    it.periode.tom == sykmeldingPeriode.tom
                            }
                            ?.journalpostId
                    sykmelding.copy(
                        journalpostId = journalpostId,
                    )
                }
            val oppdatertSykmeldingsOpplysninger =
                sykmeldingsOpplysninger.copy(sykmeldinger = oppdatertSykmeldinger)
            call.respond(HttpStatusCode.OK, oppdatertSykmeldingsOpplysninger)
        } catch (exception: JournalposterNotFoundException) {
            logger.error("Kastet exception ved henting av journalposter fra saf-api", exception)
            call.respond(HttpStatusCode.OK, sykmeldingsOpplysninger)
        } catch (exception: Exception) {
            logger.error("Kastet exception ved henting av sykmeldingsopplysninger", exception)
            call.respond(
                HttpStatusCode.InternalServerError,
                HttpMessage("Noe gikk galt ved henting av sykmeldingsopplysninger")
            )
        }
    }
}
