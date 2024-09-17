package no.nav.syfo.altinn

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.registerAltinnStatusApi() {
    val altinnStatusService by inject<AltinnStatusService>()
    get("/altinnstatus") {
        val sykmeldingId = call.request.headers["sykmeldingId"]
        val orgnummer = call.request.headers["orgnummer"]
        if (sykmeldingId.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Mangler sykmeldingId i header")
            return@get
        }
        if (orgnummer.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Mangler orgnummer i header")
            return@get
        }

        val altinnStatus = altinnStatusService.getAltinnStatus(sykmeldingId,orgnummer)

        call.respond(altinnStatus)
    }
}
