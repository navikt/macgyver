package no.nav.syfo.db.gcp

data class GcpDatabaseCredentials(
    val connectionName: String,
    val password: String,
    val username: String
)
