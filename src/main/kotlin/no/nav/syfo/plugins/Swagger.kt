package no.nav.syfo.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import no.nav.syfo.utils.EnvironmentVariables

fun Application.configureSwagger() {
    val environmentVariables = EnvironmentVariables()

    if (environmentVariables.clusterName == "dev-gcp") {
        routing { swaggerUI(path = "docs", swaggerFile = "openapi/documentation.yaml") }
    }
}
