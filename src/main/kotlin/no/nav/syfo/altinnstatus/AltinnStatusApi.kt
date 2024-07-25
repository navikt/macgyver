package no.nav.syfo.altinnstatus

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.getAltinnStatusApi() {
    val altinnStatusService by inject<AltinnStatusService>()
    get("/altinnstatus") {
        val sykmeldingId = call.request.headers["sykmeldingId"]
        if (sykmeldingId.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Mangler sykmeldingId i header")
            return@get
        }
    // skal token hentes fra backend eller fra header? Er dette rett?
        val token = call.request.headers["Authorization"]
        val altinnStatus = altinnStatusService.getAltinnStatus(sykmeldingId, token)

        call.respond(altinnStatus)
    }
}
