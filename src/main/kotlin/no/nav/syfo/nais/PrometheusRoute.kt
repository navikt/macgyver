package no.nav.syfo.nais

import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry

fun Route.naisPrometheusRoute(appRegistry: PrometheusMeterRegistry) {
    get("/metrics") {
       call.respond(appRegistry.scrape())
    }
}
