package no.nav.syfo.narmesteleder

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import java.time.LocalDate
import no.nav.syfo.clients.AccessTokenClientV2
import no.nav.syfo.logging.logger

interface NarmestelederClient {
    suspend fun getNarmesteledere(fnr: String): List<NarmesteLeder>

    suspend fun getNarmestelederKoblingerForLeder(lederFnr: String): List<NarmesteLeder>
}

class ProductionNarmestelederClient(
    private val httpClient: HttpClient,
    private val accessTokenClientV2: AccessTokenClientV2,
    private val baseUrl: String,
    private val resource: String,
) : NarmestelederClient {

    override suspend fun getNarmesteledere(fnr: String): List<NarmesteLeder> {
        try {
            val token = accessTokenClientV2.getAccessTokenV2(resource)
            return httpClient
                .get("$baseUrl/sykmeldt/narmesteledere") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                        append("Sykmeldt-Fnr", fnr)
                    }
                    accept(ContentType.Application.Json)
                }
                .body<List<NarmesteLeder>>()
        } catch (e: Exception) {
            logger.error("Noe gikk galt ved henting av nærmeste leder")
            throw e
        }
    }

    override suspend fun getNarmestelederKoblingerForLeder(lederFnr: String): List<NarmesteLeder> {
        try {
            val token = accessTokenClientV2.getAccessTokenV2(resource)
            return httpClient
                .get("$baseUrl/leder/narmesteleder/aktive") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                        append("Narmeste-Leder-Fnr", lederFnr)
                    }
                    accept(ContentType.Application.Json)
                }
                .body<List<NarmesteLeder>>()
        } catch (e: Exception) {
            logger.error("Noe gikk galt ved henting av nærmesteleder-koblinger for leder")
            throw e
        }
    }
}

class DevelopmentNarmestelederClient : NarmestelederClient {
    override suspend fun getNarmesteledere(fnr: String): List<NarmesteLeder> {
        return listOf(
            NarmesteLeder(
                fnr = fnr,
                narmesteLederFnr = "22222222222",
                orgnummer = "orgnummer",
                narmesteLederTelefonnummer = "tlf",
                narmesteLederEpost = "epost@nav.no",
                aktivFom = LocalDate.now(),
                aktivTom = LocalDate.now(),
                arbeidsgiverForskutterer = null,
                narmesteLederId = "123456",
            ),
        )
    }

    override suspend fun getNarmestelederKoblingerForLeder(lederFnr: String): List<NarmesteLeder> {
        return listOf(
            NarmesteLeder(
                fnr = "11111111111",
                narmesteLederFnr = "22222222222",
                orgnummer = "orgnummer",
                narmesteLederTelefonnummer = "tlf",
                narmesteLederEpost = "epost@nav.no",
                aktivFom = LocalDate.now(),
                aktivTom = LocalDate.now(),
                arbeidsgiverForskutterer = null,
                narmesteLederId = "123456",
            ),
        )
    }
}

data class NarmesteLeder(
    val narmesteLederId: String,
    val fnr: String,
    val narmesteLederFnr: String,
    val orgnummer: String,
    val narmesteLederTelefonnummer: String,
    val narmesteLederEpost: String,
    val aktivFom: LocalDate,
    val aktivTom: LocalDate?,
    val arbeidsgiverForskutterer: Boolean?,
)
