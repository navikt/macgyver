package no.nav.syfo.plugins

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.net.URI
import java.time.Duration
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.logging.logger
import no.nav.syfo.utils.EnvironmentVariables
import org.koin.ktor.ext.inject

fun Application.configureAuth() {
    val config by inject<AuthConfiguration>()

    if (environment.developmentMode) {
        logger.warn("In development mode! Setting up passthrough for jwt")
        install(Authentication) {
            jwt(name = "jwt") {
                verifier { null }
                validate { credentials -> JWTPrincipal(credentials.payload) }
            }
        }
        return
    }

    install(Authentication) {
        jwt(name = "jwt") {
            verifier(config.jwkProvider, config.issuer)
            validate { credentials ->
                when {
                    credentials.payload.audience.contains(config.clientId) -> {
                        val email = credentials.payload.getClaim("preferred_username").asString()
                        requireNotNull(email) {
                            "Logged in user without preferred_username should not be possible. Are you wonderwalling?"
                        }

                        UserPrincipal(
                            email = email,
                        )
                    }
                    else -> {
                        unauthorized(credentials)
                    }
                }
            }
        }
    }
}

data class UserPrincipal(val email: String) : Principal

class AuthConfiguration(
    val jwkProvider: JwkProvider,
    val issuer: String,
    val clientId: String,
)

fun getProductionAuthConfig(env: EnvironmentVariables): AuthConfiguration {
    val jwkProvider =
        JwkProviderBuilder(URI.create(env.jwkKeysUrl).toURL())
            .cached(10, Duration.ofHours(24))
            .build()

    return AuthConfiguration(
        jwkProvider = jwkProvider,
        issuer = env.jwtIssuer,
        clientId = env.clientIdV2,
    )
}

internal fun unauthorized(credentials: JWTCredential): UserPrincipal? {
    logger.warn(
        "Auth: Unexpected audience for jwt {}, {}",
        StructuredArguments.keyValue("issuer", credentials.payload.issuer),
        StructuredArguments.keyValue("audience", credentials.payload.audience),
    )
    return null
}
