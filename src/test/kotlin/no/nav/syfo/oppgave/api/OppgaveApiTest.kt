package no.nav.syfo.oppgave.api

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import java.time.LocalDate
import no.nav.syfo.oppgave.Oppgave
import no.nav.syfo.oppgave.OppgaveClient
import no.nav.syfo.oppgave.registerHentOppgaverApi
import no.nav.syfo.utils.generateJWT
import no.nav.syfo.utils.objectMapper
import no.nav.syfo.utils.setupTestApplication
import no.nav.syfo.utils.testClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import org.koin.dsl.module

internal class OppgaveApiTest {
    @AfterEach fun cleanup() = stopKoin()

    @Test
    internal fun `Test endre fnr`() = testApplication {
        val oppgaveClient = mockk<OppgaveClient>()
        setupTestApplication {
            dependencies { modules(module { single { oppgaveClient } }) }
            authedRoutes { registerHentOppgaverApi() }
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
            testClient().post("/api/oppgave/list") {
                headers {
                    append("Content-Type", "application/json")
                    append(HttpHeaders.Authorization, "Bearer ${generateJWT("2", "clientId")}")
                }
                setBody(objectMapper.writeValueAsString(oppgaverid))
            }

        assertEquals(response.status, HttpStatusCode.OK)
    }
}
