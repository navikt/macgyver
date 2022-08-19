package no.nav.syfo.db.gcp

import java.sql.Connection

interface DatabaseInterface {
    val connection: Connection
}
