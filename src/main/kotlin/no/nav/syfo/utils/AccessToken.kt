package no.nav.syfo.utils

import com.auth0.jwt.JWT
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.auth.parseAuthorizationHeader
import io.ktor.server.request.ApplicationRequest
import no.nav.syfo.log
import java.io.IOException
import java.net.URISyntaxException

@Throws(IOException::class, URISyntaxException::class)

fun getAccessTokenFromAuthHeader(request: ApplicationRequest): String {
    val authHeader = request.parseAuthorizationHeader()
        ?: throw UnauthorizedException()
    return (authHeader as HttpAuthHeader.Single).blob
}

fun logNAVEpostAndAction(accessToken: String, action: String) {
    val decodedJWT = JWT.decode(accessToken)
    val navEpost = decodedJWT.claims["preferred_username"]?.asString()
    log.info("NavEpost: {}, prøver å gjøre følgende {}", navEpost, action)
}

class UnauthorizedException : Exception()
