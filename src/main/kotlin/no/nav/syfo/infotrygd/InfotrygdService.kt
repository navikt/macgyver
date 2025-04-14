package no.nav.syfo.infotrygd

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.headers
import no.nav.syfo.clients.AccessTokenClientV2
import no.nav.syfo.logging.logger

class InfotrygdService(
    val infotrygdClient: HttpClient, private val accessTokenClientV2: AccessTokenClientV2,
    private val baseUrl: String,
    private val scope: String,
) {
    suspend fun getInfotrygdResponse(query: InfotrygdQuery): InfotrygdResponse {
        val token = accessTokenClientV2.getAccessTokenV2(scope)
        logger.info("getting infotrygd info ${query.traceId}")
        return infotrygdClient.get("$baseUrl/api/infotrygd") {
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(query)
        }.body()
    }
}
