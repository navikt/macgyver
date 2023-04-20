package no.nav.syfo.application

import com.auth0.jwk.JwkProvider
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.Principal
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.log

fun Application.setupAuth(
    jwkProvider: JwkProvider,
    issuer: String,
    clientIdV2: String,
) {
    install(Authentication) {
        jwt(name = "jwt") {
            verifier(jwkProvider, issuer)
            validate { credentials ->
                when {
                    hasMacgyverClientIdAudience(credentials, clientIdV2) -> JWTPrincipal(credentials.payload)
                    else -> {
                        unauthorized(credentials)
                    }
                }
            }
        }
    }
}

fun unauthorized(credentials: JWTCredential): Principal? {
    log.warn(
        "Auth: Unexpected audience for jwt {}, {}",
        StructuredArguments.keyValue("issuer", credentials.payload.issuer),
        StructuredArguments.keyValue("audience", credentials.payload.audience),
    )
    return null
}

fun hasMacgyverClientIdAudience(credentials: JWTCredential, clientIdV2: String): Boolean {
    return credentials.payload.audience.contains(clientIdV2)
}
