package no.nav.syfo.plugins

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.net.URI
import java.time.Duration
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.utils.EnvironmentVariables
import no.nav.syfo.utils.logger

fun Application.configureAuth() {
    val environmentVariables = EnvironmentVariables()
    val jwkProvider =
        JwkProviderBuilder(URI.create(environmentVariables.jwkKeysUrl).toURL())
            .cached(
                10,
                Duration.ofHours(24),
            )
            .build()
    val issuer = environmentVariables.jwtIssuer
    val clientIdV2 = environmentVariables.clientIdV2

    install(Authentication) {
        jwt(name = "jwt") {
            verifier(jwkProvider, issuer)
            validate { credentials ->
                when {
                    hasMacgyverClientIdAudience(
                        credentials,
                        clientIdV2,
                    ) -> JWTPrincipal(credentials.payload)
                    else -> {
                        unauthorized(credentials)
                    }
                }
            }
        }
    }
}

internal fun hasMacgyverClientIdAudience(credentials: JWTCredential, clientIdV2: String): Boolean {
    return credentials.payload.audience.contains(clientIdV2)
}

internal fun unauthorized(credentials: JWTCredential): Principal? {
    logger.warn(
        "Auth: Unexpected audience for jwt {}, {}",
        StructuredArguments.keyValue("issuer", credentials.payload.issuer),
        StructuredArguments.keyValue("audience", credentials.payload.audience),
    )
    return null
}
