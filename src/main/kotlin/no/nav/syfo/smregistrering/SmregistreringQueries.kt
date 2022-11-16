package no.nav.syfo.smregistrering

import no.nav.syfo.db.Database
import no.nav.syfo.db.toList

fun Database.getApenRegistreringsoppgave(journalpostId: String): Int? =
    connection.use { connection ->
        connection.prepareStatement(
            """
                SELECT oppgave_id
                FROM MANUELLOPPGAVE 
                WHERE journalpost_id = ? 
                AND ferdigstilt = false;
                """
        ).use {
            it.setString(1, journalpostId)
            it.executeQuery().toList { it.resultSet.getInt("oppgave_id") }.firstOrNull()
        }
    }

fun Database.ferdigstillRegistreringsoppgave(oppgaveId: Int) {
    connection.use { connection ->
        connection.prepareStatement(
            """
                UPDATE MANUELLOPPGAVE 
                SET ferdigstilt = true
                WHERE oppgave_id = ?;
                """
        ).use {
            it.setInt(1, oppgaveId)
            it.executeUpdate()
        }
        connection.commit()
    }
}
