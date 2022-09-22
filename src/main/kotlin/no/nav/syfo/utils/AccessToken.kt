package no.nav.syfo.utils

import com.auth0.jwt.JWT
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.auth.parseAuthorizationHeader
import io.ktor.server.request.ApplicationRequest
import java.io.IOException
import java.net.URISyntaxException
import no.nav.syfo.log

@Throws(IOException::class, URISyntaxException::class)

fun getAccessTokenFromAuthHeader(request: ApplicationRequest): String {
    val authHeader = request.parseAuthorizationHeader()
        ?: throw UnauthorizedException()
    return (authHeader as HttpAuthHeader.Single).blob
}

fun logNAVEpostFromTokenToSecureLogs(accessToken: String, message: String) {
    try {
        val decodedJWT = JWT.decode(accessToken)
        val navEpost = decodedJWT.claims["preferred_username"]?.asString()
        log.info("NavEpost: {} , prøver å gjøre følgende {}", navEpost, message)

    } catch (exception: Exception) {
        log.error("Fikk ikkje hentet ut navEpost", exception)
    }
}

class UnauthorizedException : Exception()