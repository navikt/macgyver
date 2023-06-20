package no.nav.syfo.smregistrering

import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import no.nav.syfo.db.Database
import no.nav.syfo.oppgave.client.Oppgave
import no.nav.syfo.oppgave.client.OppgaveClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SmregistreringServiceTest {
    private val oppgaveClient = mockk<OppgaveClient>(relaxed = true)
    private val database = mockk<Database>(relaxed = true)
    private val smregistreringService = SmregistreringService(oppgaveClient, database)

    private val journalpostId = "3434"
    private val oppgaveId = 1234

    @BeforeEach
    fun before() {
        mockkStatic("no.nav.syfo.smregistrering.SmregistreringQueriesKt")
        clearMocks(oppgaveClient, database)
    }

    @Test
    fun ferdigstillOppgaveFerdigstillerIOppgaveogSmreg() {
        runBlocking {
            every { database.getApenRegistreringsoppgave(journalpostId) } returns oppgaveId
            coEvery { oppgaveClient.hentOppgave(oppgaveId, any()) } returns
                Oppgave(
                    id = oppgaveId,
                    versjon = 1,
                    journalpostId = journalpostId,
                    beskrivelse = "Oppgaven skal slettes",
                    tema = "SYM",
                    oppgavetype = "JFR",
                    status = "AAPNET",
                    aktivDato = LocalDate.now(),
                    prioritet = "HOY",
                    tilordnetRessurs = "X121212",
                )

            smregistreringService.ferdigstillOppgave(journalpostId, "Z989898")

            verify { database.slettRegistreringsoppgave(oppgaveId) }
            coVerify { oppgaveClient.hentOppgave(oppgaveId, journalpostId) }
            coVerify {
                oppgaveClient.ferdigstillOppgave(
                    match {
                        it.status == "FERDIGSTILT" &&
                            it.tilordnetRessurs == "Z989898" &&
                            it.tildeltEnhetsnr == "2822"
                    },
                    journalpostId
                )
            }
        }
    }

    @Test
    fun ferdigstillOppgaveFerdigstillerIkkeHvisOppgavenErFerdigstilt() {
        runBlocking {
            every { database.getApenRegistreringsoppgave(journalpostId) } returns null

            smregistreringService.ferdigstillOppgave(journalpostId, "Z989898")

            verify(exactly = 0) { database.slettRegistreringsoppgave(any()) }
            coVerify(exactly = 0) { oppgaveClient.hentOppgave(any(), any()) }
            coVerify(exactly = 0) { oppgaveClient.ferdigstillOppgave(any(), any()) }
        }
    }

    @Test
    fun riktigBeskrivelseForOppgaveUtenEksisterendeBeskrivelse() {
        val tidspunkt = LocalDateTime.of(2022, 11, 25, 15, 30, 0)
        val beskrivelse =
            smregistreringService.getBeskrivelse(
                opprinneligBeskrivelse = null,
                ferdigstiltAv = "Z989898",
                tidspunkt = tidspunkt,
            )
        assertEquals(
            "--- 25.11.2022 15:30 Z989898, 2822 ---\nSykmelding er slettet og oppgaven lukket.\n\n",
            beskrivelse
        )
    }

    @Test
    fun riktigBeskrivelseForOppgaveMedEksisterendeBeskrivelse() {
        val tidspunkt = LocalDateTime.of(2022, 11, 25, 15, 30, 0)
        val beskrivelse =
            smregistreringService.getBeskrivelse(
                opprinneligBeskrivelse =
                    "--- 20.11.2022 15:00 srvsmreg, 9999 ---\nOpprettet registreringsoppgave.",
                ferdigstiltAv = "Z989898",
                tidspunkt = tidspunkt,
            )
        assertEquals(
            "--- 25.11.2022 15:30 Z989898, 2822 ---\nSykmelding er slettet og oppgaven lukket.\n\n --- 20.11.2022 15:00 srvsmreg, 9999 ---\nOpprettet registreringsoppgave.",
            beskrivelse
        )
    }
}
