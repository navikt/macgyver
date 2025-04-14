package no.nav.syfo.infotrygd

import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.inject

fun Route.registerInfotrygdApi() {
    val infotrygdService by inject<InfotrygdService>()
    get("/infotrygd") {
        val query = call.receive<InfotrygdQuery>()

        val response = infotrygdService.getInfotrygdResponse(query)

        call.respond(response)
    }
}
