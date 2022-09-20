package no.nav.syfo.application

import com.auth0.jwk.JwkProvider
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.Principal
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.request.header
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.log

fun Application.setupAuth(
    jwkProviderTokenX: JwkProvider,
    tokenXIssuer: String,
    clientIdTokenX: String
) {
    install(Authentication) {
        jwt(name = "tokenx") {
            authHeader {
                when (val token: String? = it.getToken()) {
                    null -> return@authHeader null
                    else -> return@authHeader HttpAuthHeader.Single("Bearer", token)
                }
            }
            verifier(jwkProviderTokenX, tokenXIssuer)
            validate { credentials ->
                when {
                    hasClientIdAudience(credentials, clientIdTokenX) ->
                        {
                            val principal = JWTPrincipal(credentials.payload)
                            BrukerPrincipal(
                                principal = principal,
                                token = this.getToken()!!
                            )
                        }
                    else -> unauthorized(credentials)
                }
            }
        }
    }
}

fun unauthorized(credentials: JWTCredential): Principal? {
    log.warn(
        "Auth: Unexpected audience for jwt {}, {}",
        StructuredArguments.keyValue("issuer", credentials.payload.issuer),
        StructuredArguments.keyValue("audience", credentials.payload.audience)
    )
    return null
}

fun ApplicationCall.getToken(): String? {
    return when (val authHeader = request.header("Authorization")) {
        else -> authHeader?.removePrefix("Bearer ")
    }
}

fun hasClientIdAudience(credentials: JWTCredential, clientId: String): Boolean {
    return credentials.payload.audience.contains(clientId)
}

data class BrukerPrincipal(
    val principal: JWTPrincipal,
    val token: String
) : Principal
