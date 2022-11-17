package no.nav.syfo.smregistrering

import no.nav.syfo.db.Database
import no.nav.syfo.log
import no.nav.syfo.oppgave.client.FerdigstillOppgave
import no.nav.syfo.oppgave.client.OppgaveClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SmregistreringService(
    private val oppgaveClient: OppgaveClient,
    private val databasePostgres: Database
) {
    suspend fun ferdigstillOppgave(journalpostId: String, ferdigstiltAv: String) {
        val oppgaveId = databasePostgres.getApenRegistreringsoppgave(journalpostId)

        if (oppgaveId == null) {
            log.info("Oppgave for journalpostid $journalpostId finnes ikke eller er allerede ferdigstilt")
            return
        }

        log.info("Henter oppgave med id $oppgaveId for journalpostid $journalpostId")
        val oppgave = oppgaveClient.hentOppgave(oppgaveId, journalpostId)
        oppgaveClient.ferdigstillOppgave(
            ferdigstilloppgave = FerdigstillOppgave(
                id = oppgaveId,
                versjon = oppgave.versjon
                    ?: throw RuntimeException("Fant ikke versjon for oppgave ${oppgave.id}, journalpostId $journalpostId"),
                status = "FERDIGSTILT",
                tilordnetRessurs = ferdigstiltAv,
                tildeltEnhetsnr = "2822",
                mappeId = null,
                beskrivelse = getBeskrivelse(
                    opprinneligBeskrivelse = oppgave.beskrivelse,
                    ferdigstiltAv = ferdigstiltAv
                )
            ),
            journalpostId = journalpostId
        )
        databasePostgres.ferdigstillRegistreringsoppgave(oppgaveId)
        log.info("Ferdigstilt oppgave for journalpostId $journalpostId")
    }

    fun getBeskrivelse(
        opprinneligBeskrivelse: String?,
        ferdigstiltAv: String,
        tidspunkt: LocalDateTime = LocalDateTime.now()
    ): String {
        val endringsbeskrivelse = "--- ${formaterDato(tidspunkt)} $ferdigstiltAv, 2822 ---\nSykmelding er slettet og oppgaven lukket.\n\n"
        return if (opprinneligBeskrivelse.isNullOrEmpty()) {
            endringsbeskrivelse
        } else {
            "$endringsbeskrivelse $opprinneligBeskrivelse"
        }
    }

    private fun formaterDato(dato: LocalDateTime): String {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        return dato.format(formatter)
    }
}
