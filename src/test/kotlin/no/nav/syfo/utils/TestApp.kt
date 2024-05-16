package no.nav.syfo.utils

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.testing.*
import java.nio.file.Paths
import no.nav.syfo.plugins.configureContentNegotiation
import no.nav.syfo.plugins.hasMacgyverClientIdAudience
import no.nav.syfo.plugins.unauthorized

fun ApplicationTestBuilder.configureTestAuth(
    issuer: String = "tokenxissuer",
    clientId: String = "clientId",
) {
    val path = "src/test/resources/jwkset.json"
    val uri = Paths.get(path).toUri().toURL()
    val jwkProvider = JwkProviderBuilder(uri).build()

    application {
        install(Authentication) {
            jwt(name = "jwt") {
                verifier(jwkProvider, issuer)
                validate { credentials ->
                    when {
                        hasMacgyverClientIdAudience(
                            credentials,
                            clientId,
                        ) -> JWTPrincipal(credentials.payload)
                        else -> {
                            unauthorized(credentials)
                        }
                    }
                }
            }
        }
    }
}

fun ApplicationTestBuilder.setupTestApplication() {
    application { configureContentNegotiation() }
}
