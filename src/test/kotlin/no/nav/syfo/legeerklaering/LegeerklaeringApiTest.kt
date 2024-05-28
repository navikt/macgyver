package no.nav.syfo.legeerklaering

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.utils.generateJWT
import no.nav.syfo.utils.setupTestApplication
import no.nav.syfo.utils.testClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import org.koin.dsl.module

internal class LegeerklaeringApiTest {

    @AfterEach fun cleanup() = stopKoin()

    @Test
    internal fun `should return OK`() = testApplication {
        val deleteLegeerklaeringServiceMock = mockk<DeleteLegeerklaeringService>()

        setupTestApplication {
            dependencies { modules(module { single { deleteLegeerklaeringServiceMock } }) }
            authedRoutes { registerDeleteLegeerklaeringApi() }
        }

        val legeerklaeringId = "83919f4a-f892-4db2-9255-f3c917bd012t"
        coEvery { deleteLegeerklaeringServiceMock.deleteLegeerklaering(legeerklaeringId) } returns
            Unit

        val response =
            testClient().delete("/api/legeerklaering/$legeerklaeringId") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer ${generateJWT("2", "clientId")}")
                }
            }
        assertEquals(response.status, HttpStatusCode.OK)

        val result = response.body<HttpMessage>()
        assertEquals(result.message, "Vellykket sletting")
    }

    @Test
    internal fun `should return unauthorized when missing authorization header`() =
        testApplication {
            val deleteLegeerklaeringServiceMock = mockk<DeleteLegeerklaeringService>()
            setupTestApplication {
                dependencies { module { single { deleteLegeerklaeringServiceMock } } }
                invalidRoutes { registerDeleteLegeerklaeringApi() }
            }

            val response =
                testClient().delete("/api/legeerklaering/83919f4a-f892-4db2-9255-f3c917bd012t") {}

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
}
