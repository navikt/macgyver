package no.nav.syfo.utils

import com.auth0.jwt.JWT
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.auth.parseAuthorizationHeader
import io.ktor.server.request.ApplicationRequest
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URISyntaxException

val sikkerlogg = LoggerFactory.getLogger("securelog")

@Throws(IOException::class, URISyntaxException::class)

fun getAccessTokenFromAuthHeader(request: ApplicationRequest): String {
    val authHeader = request.parseAuthorizationHeader()
        ?: throw UnauthorizedException()
    return (authHeader as HttpAuthHeader.Single).blob
}

fun getNavIdentFromToken(accessToken: String): String {
    val decodedJWT = JWT.decode(accessToken)
    return requireNotNull(decodedJWT.claims["NAVident"]?.asString()) { "NAVident mangler i token" }
}

fun logNAVEpostAndActionToSecureLog(accessToken: String, action: String) {
    val decodedJWT = JWT.decode(accessToken)
    val navEpost = decodedJWT.claims["preferred_username"]?.asString()
    sikkerlogg.info("NavEpost: {}, prøver å gjøre følgende {}", navEpost, action)
}

class UnauthorizedException : Exception()
