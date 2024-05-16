package no.nav.syfo.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import no.nav.syfo.nais.naisIsAliveRoute
import no.nav.syfo.nais.naisIsReadyRoute
import no.nav.syfo.nais.naisPrometheusRoute
import org.koin.ktor.ext.inject

fun Application.configureNaisResources() {
    val state by inject<ApplicationState>()

    routing {
        route("/internal") {
            naisIsAliveRoute(state)
            naisIsReadyRoute(state)
            naisPrometheusRoute()
        }
    }
}
