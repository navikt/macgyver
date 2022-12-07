package no.nav.syfo.sykmelding.gamlesykmeldinger.db

import no.nav.syfo.db.toList
import java.sql.Connection

fun Connection.getGamleSykmeldingIds(): List<String> =
    use { connection ->
        connection.prepareStatement(
            """
                SELECT id from sykmeldingsopplysninger
                WHERE mottatt_tidspunkt < '2020-01-01 14:27:00.000000';
            """
        ).use {
            it.executeQuery().toList { getString("id") }
        }
    }
