package no.nav.syfo.legeerklaering.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.syfo.legeerklaering.service.DeleteLegeerklaeringService
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.utils.configureTestAuth
import no.nav.syfo.utils.createTestHttpClient
import no.nav.syfo.utils.generateJWT
import no.nav.syfo.utils.setupTestApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LegeerklaeringApiTest {
    @Test
    internal fun `should return OK`() = testApplication {
        setupTestApplication()
        configureTestAuth()
        val client = createTestHttpClient()

        val deleteLegeerklaeringServiceMock = mockk<DeleteLegeerklaeringService>()

        routing {
            registerDeleteLegeerklaeringApi(
                deleteLegeerklaeringServiceMock,
            )
        }

        val legeerklaeringId = "83919f4a-f892-4db2-9255-f3c917bd012t"

        coEvery { deleteLegeerklaeringServiceMock.deleteLegeerklaering(any()) } returns Unit

        val response =
            client.delete("/api/legeerklaering/$legeerklaeringId") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer ${generateJWT("2", "clientId")}")
                }
            }
        val result = response.body<HttpMessage>()

        assertEquals(response.status, HttpStatusCode.OK)
        assertEquals(result.message, "Vellykket sletting")
    }

    @Test
    internal fun `should return unauthorized when missing authorization header`() =
        testApplication {
            setupTestApplication()
            configureTestAuth()
            val client = createTestHttpClient()

            val deleteLegeerklaeringServiceMock = mockk<DeleteLegeerklaeringService>()

            routing {
                registerDeleteLegeerklaeringApi(
                    deleteLegeerklaeringServiceMock,
                )
            }

            val response =
                client.delete("/api/legeerklaering/83919f4a-f892-4db2-9255-f3c917bd012t") {}
            val result = response.body<HttpMessage>()

            assertEquals(response.status, HttpStatusCode.Unauthorized)
            assertEquals(result.message, "Unauthorized")
        }
}
