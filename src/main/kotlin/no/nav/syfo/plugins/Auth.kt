package no.nav.syfo.plugins

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.net.URI
import java.time.Duration
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.utils.EnvironmentVariables
import no.nav.syfo.utils.logger
import org.koin.ktor.ext.inject

fun Application.configureAuth() {
    val config by inject<AuthConfiguration>()

    install(Authentication) {
        jwt(name = "jwt") {
            verifier(config.jwkProvider, config.issuer)
            validate { credentials ->
                when {
                    credentials.payload.audience.contains(config.clinetId) ->
                        JWTPrincipal(
                            credentials.payload,
                        )
                    else -> {
                        unauthorized(credentials)
                    }
                }
            }
        }
    }
}

class AuthConfiguration(
    val jwkProvider: JwkProvider,
    val issuer: String,
    val clinetId: String,
)

fun getProductionAuthConfig(env: EnvironmentVariables): AuthConfiguration {
    val jwkProvider =
        JwkProviderBuilder(URI.create(env.jwkKeysUrl).toURL())
            .cached(10, Duration.ofHours(24))
            .build()

    return AuthConfiguration(
        jwkProvider = jwkProvider,
        issuer = env.jwtIssuer,
        clinetId = env.clientIdV2,
    )
}

internal fun unauthorized(credentials: JWTCredential): Principal? {
    logger.warn(
        "Auth: Unexpected audience for jwt {}, {}",
        StructuredArguments.keyValue("issuer", credentials.payload.issuer),
        StructuredArguments.keyValue("audience", credentials.payload.audience),
    )
    return null
}
