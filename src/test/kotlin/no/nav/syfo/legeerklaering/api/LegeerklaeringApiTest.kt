package no.nav.syfo.legeerklaering.api

import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.mockk.coEvery
import io.mockk.mockk
import java.nio.file.Paths
import no.nav.syfo.application.HttpMessage
import no.nav.syfo.application.setupAuth
import no.nav.syfo.legeerklaering.service.DeleteLegeerklaeringService
import no.nav.syfo.objectMapper
import no.nav.syfo.testutil.generateJWT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LegeerklaeringApiTest {
    @Test
    internal fun `should return OK`() {
        with(TestApplicationEngine()) {
            val path = "src/test/resources/jwkset.json"
            val uri = Paths.get(path).toUri().toURL()
            val jwkProvider = JwkProviderBuilder(uri).build()
            start()

            val deleteLegeerklaeringServiceMock = mockk<DeleteLegeerklaeringService>()

            application.setupAuth(
                jwkProvider,
                "issuer",
                "clientId",
            )

            application.routing {
                registerDeleteLegeerklaeringApi(
                    deleteLegeerklaeringServiceMock,
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

            val legeerklaeringId = "83919f4a-f892-4db2-9255-f3c917bd012t"

            coEvery { deleteLegeerklaeringServiceMock.deleteLegeerklaering(any()) } returns Unit

            with(
                handleRequest(HttpMethod.Delete, "/api/legeerklaering/$legeerklaeringId") {
                    addHeader(HttpHeaders.Authorization, "Bearer ${generateJWT("2", "clientId")}")
                },
            ) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(
                    objectMapper.writeValueAsString(HttpMessage("Vellykket sletting")),
                    response.content
                )
            }
        }
    }

    @Test
    internal fun `should return unauthorized when missing authorization header`() {
        with(TestApplicationEngine()) {
            val path = "src/test/resources/jwkset.json"
            val uri = Paths.get(path).toUri().toURL()
            val jwkProvider = JwkProviderBuilder(uri).build()
            start()

            val deleteLegeerklaeringServiceMock = mockk<DeleteLegeerklaeringService>()

            application.setupAuth(
                jwkProvider,
                "issuer",
                "clientId",
            )

            application.routing {
                registerDeleteLegeerklaeringApi(
                    deleteLegeerklaeringServiceMock,
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

            with(
                handleRequest(HttpMethod.Delete, "/api/legeerklaering/${null}") {},
            ) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
                assertEquals(
                    objectMapper.writeValueAsString(HttpMessage("Unauthorized")),
                    response.content
                )
            }
        }
    }
}
