package no.nav.syfo.saf.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import no.nav.syfo.saf.model.BrukerIdInput
import no.nav.syfo.saf.model.BrukerIdType
import no.nav.syfo.saf.model.GetDokumentoversiktBrukerRequest
import no.nav.syfo.saf.model.GetDokumentoversiktBrukerResponse

class SafClient(
    private val httpClient: HttpClient,
    private val basePath: String,
    private val graphQlQuery: String,
) {

    suspend fun getDokumentoversiktBruker(fnr: String, token: String): GetDokumentoversiktBrukerResponse {
        val getDokumentoversiktBrukerRequest = GetDokumentoversiktBrukerRequest(
            query = graphQlQuery,
            variables = BrukerIdInput(id = fnr, type = BrukerIdType.FNR),
        )
        return getGraphQLResponse(getDokumentoversiktBrukerRequest, token)
    }

    private suspend inline fun <reified R> getGraphQLResponse(graphQlBody: Any, token: String): R {
        return httpClient.post(basePath) {
            setBody(graphQlBody)
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, "application/json")
        }.body()
    }
}
