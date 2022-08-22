package no.nav.syfo.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.syfo.Environment
import java.sql.Connection
import java.sql.ResultSet
import java.util.Properties
import no.nav.syfo.log

class Database(env: Environment, cloudSqlInstance: String) : DatabaseInterface {
    private val dataSource: HikariDataSource
    override val connection: Connection
        get() = dataSource.connection

    init {
        val properties = Properties()
        log.info("jdbcUrl: ${env.jdbcUrl()}")
        log.info("username: ${env.databaseUsername}")
        log.info("cloudSqlInstance: $cloudSqlInstance")
        properties.setProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory")
        properties.setProperty("cloudSqlInstance", cloudSqlInstance)
        dataSource = HikariDataSource(
            HikariConfig().apply {
                dataSourceProperties = properties
                jdbcUrl = env.jdbcUrl()
                username = env.databaseUsername
                password = env.databasePassword
                maximumPoolSize = 2
                minimumIdle = 1
                isAutoCommit = false
                connectionTimeout = 30_000
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                validate()
            }
        )
    }
}

fun <T> ResultSet.toList(mapper: ResultSet.() -> T) = mutableListOf<T>().apply {
    while (next()) {
        add(mapper())
    }
}
