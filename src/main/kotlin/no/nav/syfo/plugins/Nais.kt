package no.nav.syfo.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import no.nav.syfo.nais.naisIsAliveRoute
import no.nav.syfo.nais.naisIsReadyRoute
import no.nav.syfo.nais.naisPrometheusRoute

fun Application.configureNaisResources(state: ApplicationState) {
    routing {
        route("/internal") {
            naisIsAliveRoute(state)
            naisIsReadyRoute(state)
            naisPrometheusRoute()
        }
    }
}
