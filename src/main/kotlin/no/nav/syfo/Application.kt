package no.nav.syfo

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.plugins.*
import no.nav.syfo.plugins.configureKoin
import no.nav.syfo.utils.getEnvVar

fun main() {
    embeddedServer(
            Netty,
            port = getEnvVar("APPLICATION_PORT", "8080").toInt(),
            module = Application::module,
        )
        .start(true)
}

fun Application.module() {
    configureKoin()
    configureContentNegotiation()
    configureAuth()
    configurePrometheus()
    configureSwagger()
    configureLifecycleHooks()
    configureWebApp()
    configureNaisResources()
    configureFeatures()
    configureCallLogging()
}
