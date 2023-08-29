package no.nav.syfo.identendring.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.pdl.service.PdlPersonService

fun Route.getPersonApi(pdlPersonService: PdlPersonService) {
    get("/api/person") {
        val fnr = call.request.headers["fnr"]
        if (fnr == null) {
            call.respond(HttpStatusCode.BadRequest)
        } else {
            val person = pdlPersonService.getPdlPerson(fnr)
            call.respond(person)
        }
    }
}
