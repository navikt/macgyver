package no.nav.syfo.sykmelding.api

import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import java.nio.file.Paths
import no.nav.syfo.HttpMessage
import no.nav.syfo.setupAuth
import no.nav.syfo.sykmelding.DeleteSykmeldingService
import no.nav.syfo.testutil.generateJWT
import no.nav.syfo.utils.objectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class DeletSykmeldingApiTest {

    @Test
    internal fun `Slette sykmelding`() {
        with(TestApplicationEngine()) {
            val path = "src/test/resources/jwkset.json"
            val uri = Paths.get(path).toUri().toURL()
            val jwkProvider = JwkProviderBuilder(uri).build()
            start()

            val deleteSykmeldingServiceMock = mockk<DeleteSykmeldingService>()

            application.setupAuth(
                jwkProvider,
                "issuer",
                "clientId",
            )

            application.routing {
                registerDeleteSykmeldingApi(
                    deleteSykmeldingServiceMock,
                )
            }

            application.install(ContentNegotiation) {
                jackson {
                    registerKotlinModule()
                    registerModule(JavaTimeModule())
                    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
            }

            val sykmeldingId = "83919f4a-f892-4db2-9255-f3c917bd012t"
            val journalpostId = "99349925"

            coEvery {
                deleteSykmeldingServiceMock.deleteSykmelding(
                    any(),
                    any(),
                    any(),
                )
            } returns Unit

            with(
                handleRequest(HttpMethod.Delete, "/api/sykmelding/$sykmeldingId/$journalpostId") {
                    addHeader(HttpHeaders.Authorization, "Bearer ${generateJWT("2", "clientId")}")
                },
            ) {
                Assertions.assertEquals(HttpStatusCode.OK, response.status())
                Assertions.assertEquals(
                    objectMapper.writeValueAsString(HttpMessage("Vellykket sletting")),
                    response.content,
                )
            }
        }
    }

    @Test
    internal fun `Slette sykmelding should throw not found`() {
        with(TestApplicationEngine()) {
            val path = "src/test/resources/jwkset.json"
            val uri = Paths.get(path).toUri().toURL()
            val jwkProvider = JwkProviderBuilder(uri).build()
            start()

            val deleteSykmeldingServiceMock = mockk<DeleteSykmeldingService>()

            application.setupAuth(
                jwkProvider,
                "issuer",
                "clientId",
            )

            application.routing {
                registerDeleteSykmeldingApi(
                    deleteSykmeldingServiceMock,
                )
            }

            application.install(ContentNegotiation) {
                jackson {
                    registerKotlinModule()
                    registerModule(JavaTimeModule())
                    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
            }

            val sykmeldingId = "83919f4a-f892-4db2-9255-f3c917bd012t"

            coEvery {
                deleteSykmeldingServiceMock.deleteSykmelding(
                    any(),
                    any(),
                    any(),
                )
            } returns Unit

            with(
                handleRequest(HttpMethod.Delete, "/api/sykmelding/$sykmeldingId") {
                    addHeader(HttpHeaders.Authorization, "Bearer ${generateJWT("2", "clientId")}")
                },
            ) {
                Assertions.assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }
}
