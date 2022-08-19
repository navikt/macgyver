package no.nav.syfo.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.syfo.ServiceUser
import java.sql.Connection
import java.sql.ResultSet

class DatabaseOracle(
    private val jdbcUrl: String,
    private val serviceUser: ServiceUser
) : DatabaseInterfaceOracle {

    private val dataSource: HikariDataSource

    override val connection: Connection
        get() = dataSource.connection

    init {
        dataSource = HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = jdbcUrl
                username = serviceUser.serviceuserUsername
                password = serviceUser.serviceuserPassword
                maximumPoolSize = 3
                isAutoCommit = false
                driverClassName = "oracle.jdbc.OracleDriver"
                validate()
            }
        )
    }
}

fun <T> ResultSet.toList(mapper: ResultSet.() -> T): List<T> = mutableListOf<T>().apply {
    while (next()) {
        add(mapper())
    }
}

interface DatabaseInterfaceOracle {
    val connection: Connection
}
