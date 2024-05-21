package no.nav.syfo.sykmelding.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.sykmelding.delete_sykmelding.DeleteSykmeldingService
import no.nav.syfo.sykmelding.delete_sykmelding.registerDeleteSykmeldingApi
import no.nav.syfo.utils.generateJWT
import no.nav.syfo.utils.setupTestApplication
import no.nav.syfo.utils.testClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import org.koin.dsl.module

internal class DeletSykmeldingApiTest {

    @AfterEach fun cleanup() = stopKoin()

    @Test
    internal fun `Slette sykmelding`() = testApplication {
        val deleteSykmeldingServiceMock = mockk<DeleteSykmeldingService>()
        setupTestApplication(withAuth = true) {
            modules(module { single { deleteSykmeldingServiceMock } })
        }

        routing { registerDeleteSykmeldingApi() }

        val sykmeldingId = "83919f4a-f892-4db2-9255-f3c917bd012t"
        val journalpostId = "99349925"

        coEvery {
            deleteSykmeldingServiceMock.deleteSykmelding(
                sykmeldingId,
                journalpostId,
                any(),
            )
        } returns Unit

        val response =
            testClient().delete("/api/sykmelding/$sykmeldingId/$journalpostId") {
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
        val deleteSykmeldingServiceMock = mockk<DeleteSykmeldingService>()
        setupTestApplication(withAuth = true) { module { single { deleteSykmeldingServiceMock } } }

        routing { registerDeleteSykmeldingApi() }

        val sykmeldingId = "83919f4a-f892-4db2-9255-f3c917bd012t"

        coEvery {
            deleteSykmeldingServiceMock.deleteSykmelding(
                sykmeldingId,
                any(),
                any(),
            )
        } returns Unit

        val response =
            testClient().delete("/api/sykmelding/$sykmeldingId") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer ${generateJWT("2", "clientId")}")
                }
            }

        assertEquals(response.status, HttpStatusCode.NotFound)
    }
}
