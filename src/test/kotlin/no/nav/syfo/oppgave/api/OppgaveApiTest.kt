package no.nav.syfo.oppgave.api

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.headers
import io.ktor.server.routing.routing
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import java.time.LocalDate
import no.nav.syfo.oppgave.client.Oppgave
import no.nav.syfo.oppgave.client.OppgaveClient
import no.nav.syfo.utils.configureTestAuth
import no.nav.syfo.utils.createTestHttpClient
import no.nav.syfo.utils.generateJWT
import no.nav.syfo.utils.objectMapper
import no.nav.syfo.utils.setupTestApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OppgaveApiTest {
    @Test
    internal fun `Test endre fnr`() = testApplication {
        setupTestApplication()
        configureTestAuth()
        val client = createTestHttpClient()

        val oppgaveClient = mockk<OppgaveClient>()

        mockkStatic("no.nav.syfo.identendring.db.SyfoSmRegisterKt")

        routing {
            registerHentOppgaverApi(
                oppgaveClient,
            )
        }

        coEvery { oppgaveClient.hentOppgave(any(), any()) } returns
            Oppgave(
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

        val response =
            client.post("/api/oppgave/list") {
                headers {
                    append("Content-Type", "application/json")
                    append(HttpHeaders.Authorization, "Bearer ${generateJWT("2", "clientId")}")
                }
                setBody(objectMapper.writeValueAsString(oppgaverid))
            }

        assertEquals(response.status, HttpStatusCode.OK)
    }
}
