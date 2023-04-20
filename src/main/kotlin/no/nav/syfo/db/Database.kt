package no.nav.syfo.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.ResultSet
import java.util.Properties

class Database(
    cloudSqlInstance: String,
    dbHost: String,
    dbPort: String,
    dbName: String,
    dbUsername: String,
    dbPassword: String,
) : DatabaseInterface {
    private val dataSource: HikariDataSource
    override val connection: Connection
        get() = dataSource.connection

    init {
        val properties = Properties()
        properties.setProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory")
        properties.setProperty("cloudSqlInstance", cloudSqlInstance)
        dataSource = HikariDataSource(
            HikariConfig().apply {
                dataSourceProperties = properties
                jdbcUrl = "jdbc:postgresql://$dbHost:$dbPort/$dbName"
                username = dbUsername
                password = dbPassword
                maximumPoolSize = 1
                minimumIdle = 1
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                validate()
            },
        )
    }
}

fun <T> ResultSet.toList(mapper: ResultSet.() -> T) = mutableListOf<T>().apply {
    while (next()) {
        add(mapper())
    }
}
