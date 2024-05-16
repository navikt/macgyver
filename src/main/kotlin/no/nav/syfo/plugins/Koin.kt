package no.nav.syfo.plugins

import io.ktor.server.application.*
import no.nav.syfo.utils.EnvironmentVariables
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()

        modules(
            environmentModule,
            applicationStateModule,
            authModule,
        )
    }
}

val environmentModule = module { single { EnvironmentVariables() } }

val applicationStateModule = module { single { ApplicationState() } }

val authModule = module { single { getProductionAuthConfig(get()) } }
