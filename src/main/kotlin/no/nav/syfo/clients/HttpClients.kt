package no.nav.syfo.clients

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.apache.ApacheEngineConfig
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.network.sockets.SocketTimeoutException
import io.ktor.serialization.jackson.jackson
import no.nav.syfo.EnvironmentVariables
import no.nav.syfo.identendring.client.NarmestelederClient
import no.nav.syfo.oppgave.client.OppgaveClient
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.pdl.service.PdlPersonService
import no.nav.syfo.saf.client.SafClient
import no.nav.syfo.saf.service.SafService

class HttpClients(environmentVariables: EnvironmentVariables) {
    private val config: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {
        install(HttpTimeout) {
            connectTimeoutMillis = 10000
            requestTimeoutMillis = 10000
            socketTimeoutMillis = 10000
        }
        install(ContentNegotiation) {
            jackson {
                registerKotlinModule()
                registerModule(JavaTimeModule())
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                setSerializationInclusion(JsonInclude.Include.NON_NULL)
            }
        }
        expectSuccess = false
        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, _ ->
                when (exception) {
                    is SocketTimeoutException ->
                        throw ServiceUnavailableException(exception.message)
                }
            }
        }
    }

    private val httpClient = HttpClient(Apache, config)

    private val accessTokenClientV2 =
        AccessTokenClientV2(
            environmentVariables.aadAccessTokenV2Url,
            environmentVariables.clientIdV2,
            environmentVariables.clientSecretV2,
            httpClient,
        )

    private val pdlClient =
        PdlClient(
            httpClient = httpClient,
            basePath = environmentVariables.pdlGraphqlPath,
            graphQlQuery =
                PdlClient::class
                    .java
                    .getResource("/graphql/getPerson.graphql")!!
                    .readText()
                    .replace(Regex("[\n\t]"), ""),
            graphQlQueryAktorids =
                PdlClient::class
                    .java
                    .getResource("/graphql/getAktorids.graphql")!!
                    .readText()
                    .replace(Regex("[\n\t]"), ""),
        )

    val pdlService = PdlPersonService(pdlClient, accessTokenClientV2, environmentVariables.pdlScope)

    val oppgaveClient =
        OppgaveClient(
            environmentVariables.oppgavebehandlingUrl,
            accessTokenClientV2,
            environmentVariables.oppgaveScope,
            httpClient
        )

    val narmestelederClient =
        NarmestelederClient(
            httpClient,
            accessTokenClientV2,
            environmentVariables.narmestelederUrl,
            environmentVariables.narmestelederScope
        )

    private val safClient =
        SafClient(
            httpClient = httpClient,
            basePath = environmentVariables.safGraphqlPath,
            graphQlQuery =
                SafClient::class
                    .java
                    .getResource("/graphql/dokumentoversiktBruker.graphql")!!
                    .readText()
                    .replace(Regex("[\n\t]"), ""),
        )

    val safService = SafService(safClient, accessTokenClientV2, environmentVariables.safScope)
}

class ServiceUnavailableException(message: String?) : Exception(message)
