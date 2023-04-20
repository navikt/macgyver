package no.nav.syfo.persistering.db.postgres

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.syfo.model.Merknad
import no.nav.syfo.model.Sykmeldingsdokument
import no.nav.syfo.model.Sykmeldingsopplysninger
import no.nav.syfo.model.UtenlandskSykmelding
import no.nav.syfo.objectMapper
import java.sql.Connection
import java.sql.ResultSet

fun Connection.hentSykmeldingMedId(sykmeldingId: String): SykmeldingDbModel? =
    use { connection ->
        connection.prepareStatement(
            """
                select * from sykmeldingsopplysninger sm 
                INNER JOIN sykmeldingsdokument sd on sm.id = sd.id
                WHERE sm.id = ?
            """,
        ).use {
            it.setString(1, sykmeldingId)
            it.executeQuery().toSykmelding()
        }
    }

fun ResultSet.toSykmelding(): SykmeldingDbModel? {
    if (next()) {
        val sykmeldingId = getString("id")
        val sykmeldingsdokument =
            getNullsafeSykmeldingsdokument(sykmeldingId)
        val sykmeldingsopplysninger = Sykmeldingsopplysninger(
            id = sykmeldingId,
            mottakId = getString("mottak_id"),
            pasientFnr = getString("pasient_fnr"),
            pasientAktoerId = getString("pasient_aktoer_id"),
            legeFnr = getString("lege_fnr"),
            legeAktoerId = getString("lege_aktoer_id"),
            legekontorOrgNr = getString("legekontor_org_nr"),
            legekontorHerId = getString("legekontor_her_id"),
            legekontorReshId = getString("legekontor_resh_id"),
            epjSystemNavn = getString("epj_system_navn"),
            epjSystemVersjon = getString("epj_system_versjon"),
            mottattTidspunkt = getTimestamp("mottatt_tidspunkt").toLocalDateTime(),
            tssid = getString("tss_id"),
            merknader = getString("merknader")?.let { objectMapper.readValue<List<Merknad>>(it) },
            partnerreferanse = getString("partnerreferanse"),
            utenlandskSykmelding = getString("utenlandsk_sykmelding")?.let { objectMapper.readValue<UtenlandskSykmelding>(it) },
        )
        return SykmeldingDbModel(sykmeldingsopplysninger, sykmeldingsdokument)
    }
    return null
}

private fun ResultSet.getNullsafeSykmeldingsdokument(sykmeldingId: String): Sykmeldingsdokument? {
    val sykmeldingDokument = getString("sykmelding")
    if (sykmeldingDokument.isNullOrEmpty()) {
        return null
    }
    return Sykmeldingsdokument(sykmeldingId, objectMapper.readValue(getString("sykmelding")))
}
