package no.nav.syfo.altinn

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.syfo.clients.AccessTokenClientV2
import no.nav.syfo.logging.logger

class AltinnStatusService(
    private val httpClient: HttpClient,
    private val accessTokenClientV2: AccessTokenClientV2,
    private val baseUrl: String,
    private val scope: String,
) {
    suspend fun getAltinnStatus(sykmeldingId: String, orgnummer: String): AltinnStatus {
        try {
            val token = accessTokenClientV2.getAccessTokenV2(scope)
            return httpClient
                .get("$baseUrl/internal/altinn/${sykmeldingId}/${orgnummer}") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                    accept(ContentType.Application.Json)
                }
                .body<AltinnStatus>()
        } catch (e: Exception) {
            logger.error("Noe gikk galt ved henting av nærmeste leder")
            throw e
        }
    }


}
