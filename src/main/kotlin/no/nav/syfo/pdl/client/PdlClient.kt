package no.nav.syfo.pdl.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import no.nav.syfo.pdl.client.model.GetAktoridsRequest
import no.nav.syfo.pdl.client.model.GetAktoridsResponse
import no.nav.syfo.pdl.client.model.GetAktoridsVariables
import no.nav.syfo.pdl.client.model.GetPersonRequest
import no.nav.syfo.pdl.client.model.GetPersonVariables
import no.nav.syfo.pdl.client.model.IdentInformasjon
import no.nav.syfo.pdl.client.model.Identliste
import no.nav.syfo.pdl.client.model.Navn
import no.nav.syfo.pdl.client.model.PdlResponse
import no.nav.syfo.pdl.client.model.PersonResponse
import no.nav.syfo.pdl.client.model.ResponseData
import no.nav.syfo.pdl.model.GraphQLResponse

interface PdlClient {
    suspend fun getPerson(fnr: String, token: String): GraphQLResponse<PdlResponse>

    suspend fun getFnrs(aktorids: List<String>, token: String): GetAktoridsResponse
}

class ProductionPdlClient(
    private val httpClient: HttpClient,
    private val basePath: String,
    private val graphQlQuery: String,
    private val graphQlQueryAktorids: String,
) : PdlClient {

    override suspend fun getPerson(fnr: String, token: String): GraphQLResponse<PdlResponse> {
        val getPersonRequest =
            GetPersonRequest(query = graphQlQuery, variables = GetPersonVariables(ident = fnr))
        return getGraphQLResponse(getPersonRequest, token)
    }

    override suspend fun getFnrs(aktorids: List<String>, token: String): GetAktoridsResponse {
        val getAktoridsRequest =
            GetAktoridsRequest(
                query = graphQlQueryAktorids,
                variables = GetAktoridsVariables(identer = aktorids),
            )
        return getGraphQLResponse(getAktoridsRequest, token)
    }

    private suspend inline fun <reified R> getGraphQLResponse(graphQlBody: Any, token: String): R {
        return httpClient
            .post(basePath) {
                setBody(graphQlBody)
                header(HttpHeaders.Authorization, "Bearer $token")
                header("Behandlingsnummer", "B229")
                header("TEMA", "SYM")
                header(HttpHeaders.ContentType, "application/json")
            }
            .body()
    }
}

class DevelopmentPdlClient : PdlClient {
    override suspend fun getPerson(fnr: String, token: String): GraphQLResponse<PdlResponse> {
        return GraphQLResponse(
            data =
                PdlResponse(
                    hentIdenter =
                        Identliste(
                            identer =
                                listOf(
                                    IdentInformasjon(
                                        ident = "12345678910",
                                        historisk = false,
                                        gruppe = "FOLKEREGISTERIDENT",
                                    ),
                                ),
                        ),
                    person =
                        PersonResponse(
                            listOf(
                                Navn(
                                    fornavn = "KARL",
                                    mellomnavn = "MELLONAVN",
                                    etternavn = "NORDMANN",
                                ),
                            ),
                        ),
                ),
            errors = emptyList(),
        )
    }

    override suspend fun getFnrs(aktorids: List<String>, token: String): GetAktoridsResponse {
        return GetAktoridsResponse(
            data =
                ResponseData(
                    hentIdenterBolk = emptyList(),
                ),
            errors = null,
        )
    }
}
