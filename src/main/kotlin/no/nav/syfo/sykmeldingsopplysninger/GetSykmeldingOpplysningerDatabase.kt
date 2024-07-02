package no.nav.syfo.sykmeldingsopplysninger

import com.fasterxml.jackson.module.kotlin.readValue
import java.sql.ResultSet
import java.time.LocalDate
import no.nav.syfo.db.Database
import no.nav.syfo.db.toList
import no.nav.syfo.identendring.update_fnr.StatusEvent
import no.nav.syfo.logging.logger
import no.nav.syfo.model.Merknad
import no.nav.syfo.model.RuleInfo
import no.nav.syfo.utils.objectMapper

interface GetSykmeldingOpplysningerDatabase {
    suspend fun getAlleSykmeldinger(fnr: String): List<Sykmelding>
}

class GetSykmeldingerDatabaseDevelopment() : GetSykmeldingOpplysningerDatabase {
    override suspend fun getAlleSykmeldinger(fnr: String): List<Sykmelding> {
        logger.info("Henter sykmeldinger fra dev")
        return emptyList()
    }
}

class GetSykmeldingerDatabaseProduction(val database: Database) :
    GetSykmeldingOpplysningerDatabase {

    override suspend fun getAlleSykmeldinger(fnr: String): List<Sykmelding> =
        this.database.connection
            .prepareStatement(
                """
                     select * from sykmeldingsopplysninger smo 
                     where smo.pasient_fnr = ?
                    """,
            )
            .use { statement ->
                statement.setString(1, fnr)
                statement.executeQuery().toList { toSykmelding() }
            }

    fun getArbeidsgiver(sykmeldingId: String): Arbeidsgiver =
        this.database.connection
            .prepareStatement(
                """
                     select * from arbeidsgiver arb
                     where arb.sykmelding_id = ?
                    """,
            )
            .use { statement ->
                statement.setString(1, sykmeldingId)
                statement.executeQuery().toArbeidsgiver()
            }

    private fun getBehandlingsUtfall(sykmeldingId: String): BehandlingsUtfall? =
        this.database.connection
            .prepareStatement(
                """
                     select * from behandlingsutfall beh
                     where beh.id = ?
                    """,
            )
            .use { statement ->
                statement.setString(1, sykmeldingId)
                statement.executeQuery().toBehandlingsutfall()
            }

    private fun getPerioder(sykmeldingId: String): List<Periode> =
        this.database.connection
            .prepareStatement(
                """
                     select * from sykmeldingsdokument smd
                     where smd.id = ?
                    """,
            )
            .use { statement ->
                statement.setString(1, sykmeldingId)
                statement.executeQuery().toPerioder()
            }

    private fun getSykmeldingStatus(sykmeldingId: String): String =
        this.database.connection
            .prepareStatement(
                """
                     select * from sykmeldingstatus status
                     where status.sykmelding_id = ?
                    """,
            )
            .use { statement ->
                statement.setString(1, sykmeldingId)
                statement.executeQuery().toString()
            }

    private fun ResultSet.toSykmelding(): Sykmelding {
        logger.info("Konverterer sykmelding")
        val sykmeldingsopplysninger =
            Sykmelding(
                sykmeldingId = "id",
                mottakId = getString("mottak_id"),
                mottattTidspunkt = getTimestamp("mottatt_tidspunkt").toLocalDateTime(),
                tssId = getString("tss_id"),
                //arbeidsgiver = getArbeidsgiver("id"),
                arbeidsgiver = null,
                synligStatus = null,
                behandlingsUtfall = null,
                //behandlingsUtfall = getBehandlingsUtfall("id"),
                hovedDiagnose = null,
               /*hovedDiagnose =
                getHovedDiagnose("hovedDiagnose")?.let {
                    objectMapper.readValue<HovedDiagnose>(
                        it,
                    )
                }*/
                merknader = null,
                // merknader = getString("merknader")?.let { objectMapper.readValue<List<Merknad>>(it) },
                // statusEvent = getSykmeldingStatus("id"),
                statusEvent = "APEN",
                // perioder = getPerioder("id"),
                perioder = emptyList()

            )
        return sykmeldingsopplysninger
    }

    private fun ResultSet.toArbeidsgiver(): Arbeidsgiver {
        val arbeidsgiver =
            Arbeidsgiver(
                orgnummer = getString("orgnummer"),
                orgNavn = getString("navn"),
            )
        return arbeidsgiver
    }

    private fun ResultSet.toBehandlingsutfall(): BehandlingsUtfall? {
        val behandlingsutfall =
            getString("ruleHits")
                ?.let { objectMapper.readValue<List<RuleInfo>>(it) }
                ?.let {
                    BehandlingsUtfall(
                        status = getString("status"),
                        ruleHits = it,
                    )
                }
        return behandlingsutfall
    }

    private fun ResultSet.toPerioder(): List<Periode> {

        val sykmeldingJson = getString("sykmelding") ?: return emptyList()
        val sykmelding: Sykmelding = objectMapper.readValue(sykmeldingJson)

        return sykmelding.perioder
    }

    private fun ResultSet.toHovedDiagnose(): HovedDiagnose? {
        val sykmeldingJson = this.getString("sykmelding") ?: return null
        val sykmelding: Sykmelding = objectMapper.readValue(sykmeldingJson)
        return sykmelding.hovedDiagnose

    }
}
