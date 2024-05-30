package no.nav.syfo.sykmelding.delete_sykmelding

import com.fasterxml.jackson.module.kotlin.readValue
import java.sql.ResultSet
import java.time.LocalDateTime
import no.nav.syfo.db.Database
import no.nav.syfo.logging.logger
import no.nav.syfo.model.Merknad
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.model.UtenlandskSykmelding
import no.nav.syfo.utils.objectMapper

data class Sykmeldingsopplysninger(
    var id: String,
    val pasientFnr: String,
    val pasientAktoerId: String,
    val legeFnr: String,
    val legeAktoerId: String,
    val mottakId: String,
    val legekontorOrgNr: String?,
    val legekontorHerId: String?,
    val legekontorReshId: String?,
    val epjSystemNavn: String,
    val epjSystemVersjon: String,
    var mottattTidspunkt: LocalDateTime,
    val tssid: String?,
    val merknader: List<Merknad>?,
    val partnerreferanse: String?,
    val utenlandskSykmelding: UtenlandskSykmelding?,
)

data class Sykmeldingsdokument(
    var id: String,
    var sykmelding: Sykmelding,
)

data class SykmeldingDbModel(
    val sykmeldingsopplysninger: Sykmeldingsopplysninger,
    val sykmeldingsdokument: Sykmeldingsdokument?,
)

interface DeleteSykmeldingDatabase {
    fun hentSykmeldingMedId(sykmeldingId: String): SykmeldingDbModel?
}

class DeleteSykmeldingDatabaseProduction(val database: Database) : DeleteSykmeldingDatabase {
    override fun hentSykmeldingMedId(sykmeldingId: String): SykmeldingDbModel? =
        database.connection.use { connection ->
            connection
                .prepareStatement(
                    """
                select * from sykmeldingsopplysninger sm 
                INNER JOIN sykmeldingsdokument sd on sm.id = sd.id
                WHERE sm.id = ?
            """,
                )
                .use {
                    it.setString(1, sykmeldingId)
                    it.executeQuery().toSykmelding()
                }
        }
}

class DeleteSykmeldingDatabaseDevelopment() : DeleteSykmeldingDatabase {
    override fun hentSykmeldingMedId(sykmeldingId: String): SykmeldingDbModel? {
        logger.info("Henter sykmelding med id $sykmeldingId")
        return null
    }
}

private fun ResultSet.toSykmelding(): SykmeldingDbModel? {
    if (next()) {
        val sykmeldingId = getString("id")
        val sykmeldingsdokument = getNullsafeSykmeldingsdokument(sykmeldingId)
        val sykmeldingsopplysninger =
            Sykmeldingsopplysninger(
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
                merknader =
                    getString("merknader")?.let { objectMapper.readValue<List<Merknad>>(it) },
                partnerreferanse = getString("partnerreferanse"),
                utenlandskSykmelding =
                    getString("utenlandsk_sykmelding")?.let {
                        objectMapper.readValue<UtenlandskSykmelding>(it)
                    },
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
