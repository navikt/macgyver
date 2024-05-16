package no.nav.syfo.sykmelding.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.headers
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.sykmelding.DeleteSykmeldingService
import no.nav.syfo.utils.configureTestAuth
import no.nav.syfo.utils.createTestHttpClient
import no.nav.syfo.utils.generateJWT
import no.nav.syfo.utils.setupTestApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DeletSykmeldingApiTest {

    @Test
    internal fun `Slette sykmelding`() = testApplication {
        setupTestApplication()
        configureTestAuth()
        val client = createTestHttpClient()

        val deleteSykmeldingServiceMock = mockk<DeleteSykmeldingService>()

        routing {
            registerDeleteSykmeldingApi(
                deleteSykmeldingServiceMock,
            )
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

        val response =
            client.delete("/api/sykmelding/$sykmeldingId/$journalpostId") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer ${generateJWT("2", "clientId")}")
                }
            }
        val result = response.body<HttpMessage>()

        assertEquals(response.status, HttpStatusCode.OK)
        assertEquals(result.message, "Vellykket sletting")
    }

    @Test
    internal fun `Slette sykmelding should throw not found`() = testApplication {
        setupTestApplication()
        configureTestAuth()
        val client = createTestHttpClient()

        val deleteSykmeldingServiceMock = mockk<DeleteSykmeldingService>()
        routing {
            registerDeleteSykmeldingApi(
                deleteSykmeldingServiceMock,
            )
        }

        val sykmeldingId = "83919f4a-f892-4db2-9255-f3c917bd012t"

        coEvery {
            deleteSykmeldingServiceMock.deleteSykmelding(
                any(),
                any(),
                any(),
            )
        } returns Unit

        val response =
            client.delete("/api/sykmelding/$sykmeldingId") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer ${generateJWT("2", "clientId")}")
                }
            }

        assertEquals(response.status, HttpStatusCode.NotFound)
    }
}
