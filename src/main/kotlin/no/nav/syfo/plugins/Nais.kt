package no.nav.syfo.plugins

import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.routing.*
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import no.nav.syfo.nais.naisIsAliveRoute
import no.nav.syfo.nais.naisIsReadyRoute
import no.nav.syfo.nais.naisPrometheusRoute
import org.koin.ktor.ext.inject

val appRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)


fun Application.configureNaisResources() {
    val state by inject<ApplicationState>()

    install(MicrometerMetrics) { registry = appRegistry }

    routing {
        route("/api/internal") {
            naisIsAliveRoute(state)
            naisIsReadyRoute(state)
            naisPrometheusRoute(appRegistry)
        }
    }
}
