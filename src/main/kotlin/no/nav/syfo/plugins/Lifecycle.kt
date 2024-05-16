package no.nav.syfo.plugins

import io.ktor.server.application.*

fun Application.configureLifecycleHooks(state: ApplicationState) {
    environment.monitor.subscribe(ApplicationStarted) { state.ready = true }
    environment.monitor.subscribe(ApplicationStopped) { state.ready = false }
}

data class ApplicationState(
    var alive: Boolean = true,
    var ready: Boolean = true,
)
