package no.nav.syfo.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*

fun Application.configureCallLogging() {
    install(CallLogging) { filter { call -> call.response.status()?.value != 200 } }
}
