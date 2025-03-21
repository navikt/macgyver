package no.nav.syfo.plugins

import io.ktor.server.application.*
import org.koin.ktor.ext.inject

fun Application.configureLifecycleHooks() {
    val state by inject<ApplicationState>()

    monitor.subscribe(ApplicationStarted) { state.ready = true }
    monitor.subscribe(ApplicationStopped) { state.ready = false }
}

data class ApplicationState(
    var alive: Boolean = true,
    var ready: Boolean = true,
)
