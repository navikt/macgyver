package no.nav.syfo.utils

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.testing.*
import java.nio.file.Paths
import no.nav.syfo.plugins.AuthConfiguration
import no.nav.syfo.plugins.applicationStateModule
import no.nav.syfo.plugins.configureAuth
import no.nav.syfo.plugins.configureContentNegotiation
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun ApplicationTestBuilder.configureTestAuth() {
    application { configureAuth() }
}

fun ApplicationTestBuilder.setupTestApplication(
    withAuth: Boolean = false,
    koin: KoinAppDeclaration? = null,
) {
    application { configureContentNegotiation() }

    startKoin {
        modules(applicationStateModule)
        if (withAuth) {
            modules(mockedAuthModule)
        }
        if (koin != null) {
            koin()
        }
    }
}

val mockedAuthModule = module {
    val path = "src/test/resources/jwkset.json"
    val uri = Paths.get(path).toUri().toURL()
    val jwkProvider = JwkProviderBuilder(uri).build()

    single {
        val env = get<EnvironmentVariables>()

        AuthConfiguration(
            jwkProvider = jwkProvider,
            issuer = "issuer",
            clinetId = env.clientIdV2,
        )
    }
}
