package no.nav.syfo.sykmeldingsopplysninger

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.logging.logger
import no.nav.syfo.logging.sikkerlogg
import no.nav.syfo.model.HttpMessage
import org.koin.ktor.ext.inject

fun Route.registerSykmeldingsOpplysningerApi() {
    val sykmeldingsOpplysningerClient by inject<DevelopmentSykmeldingsOpplysningerClient>()

    get("/sykmeldingsopplysninger") {
        logger.info("Henter sykmeldingsopplysninger")
        val fnr = call.request.headers["fnr"]
        sikkerlogg.info("prøver å hente journalposter på fnr $fnr")

        if (fnr.isNullOrEmpty()) {
            logger.warn("fnr kan ikke være null eller tom")
            call.respond(HttpStatusCode.BadRequest, HttpMessage("fnr kan ikke være null eller tom"))
            return@get
        }

        val sykmeldingsOpplysninger = sykmeldingsOpplysningerClient.getSykmeldingsopplysninger(fnr)
        call.respond(sykmeldingsOpplysninger)
    }
}