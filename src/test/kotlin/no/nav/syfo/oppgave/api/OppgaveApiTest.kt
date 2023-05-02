package no.nav.syfo.oppgave.api

import com.auth0.jwk.JwkProviderBuilder
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
import io.ktor.server.testing.setBody
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import no.nav.syfo.application.setupAuth
import no.nav.syfo.objectMapper
import no.nav.syfo.oppgave.client.Oppgave
import no.nav.syfo.oppgave.client.OppgaveClient
import no.nav.syfo.testutil.generateJWT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import java.time.LocalDate

internal class OppgaveApiTest {
    @Test
    internal fun `Test endre fnr`() {
        with(TestApplicationEngine()) {
            val path = "src/test/resources/jwkset.json"
            val uri = Paths.get(path).toUri().toURL()
            val jwkProvider = JwkProviderBuilder(uri).build()

            val oppgaveClient = mockk<OppgaveClient>()

            mockkStatic("no.nav.syfo.identendring.db.SyfoSmRegisterKt")

            start()

            application.setupAuth(
                jwkProvider,
                "issuer",
                "clientId",
            )
            application.routing {
                registerHentOppgaverApi(
                    oppgaveClient,
                )
            }

            application.install(ContentNegotiation) {
                jackson {
                    registerKotlinModule()
                    registerModule(JavaTimeModule())
                    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                }
            }

            coEvery { oppgaveClient.hentOppgave(any(), any()) } returns Oppgave(
                id = 1,
                versjon = null,
                tildeltEnhetsnr = null,
                opprettetAvEnhetsnr = null,
                aktoerId = null,
                journalpostId = null,
                behandlesAvApplikasjon = null,
                saksreferanse = null,
                tilordnetRessurs = null,
                beskrivelse = null,
                tema = "SYK",
                oppgavetype = "BEHANDLE SYKMELDING",
                behandlingstype = null,
                aktivDato = LocalDate.now(),
                fristFerdigstillelse = null,
                prioritet = "HØY",
                status = null,
                mappeId = null,
            )

            val oppgaverid = listOf(121321312)

            with(
                handleRequest(HttpMethod.Post, "/api/oppgave/list") {
                    addHeader("Content-Type", "application/json")
                    addHeader(HttpHeaders.Authorization, "Bearer ${generateJWT("2", "clientId")}")
                    setBody(objectMapper.writeValueAsString(oppgaverid))
                },
            ) {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }
}
