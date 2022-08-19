package no.nav.syfo.legeerklaring

import no.nav.syfo.db.gcp.GcpDatabase

fun GcpDatabase.exists(mottakId: String, msgId: String): Boolean {
    return connection.use { connection ->
        connection.prepareStatement(
            """
            select id from legeerklaeringopplysninger where msg_id = ? and mottak_id = ?;
        """
        ).use {
            it.setString(1, msgId)
            it.setString(2, mottakId)
            it.executeQuery().next()
        }
    }
}
