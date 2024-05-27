package no.nav.syfo.identendring

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.pdl.PdlPersonService
import org.koin.ktor.ext.inject

fun Route.registerPersonApi() {
    val pdlPersonService by inject<PdlPersonService>()

    get("/person") {
        val fnr = call.request.headers["fnr"]
        if (fnr == null) {
            call.respond(HttpStatusCode.BadRequest)
        } else {
            val person = pdlPersonService.getPdlPerson(fnr)
            call.respond(person)
        }
    }
}
