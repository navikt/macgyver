package no.nav.syfo.sykmeldingsopplysninger

import com.fasterxml.jackson.module.kotlin.readValue
import java.sql.ResultSet
import no.nav.syfo.db.Database
import no.nav.syfo.logging.logger
import no.nav.syfo.model.Merknad
import no.nav.syfo.utils.objectMapper

interface GetSykmeldingOpplysningerDatabase {
    suspend fun getAlleSykmeldinger(fnr: String): List<Sykmelding>
}

data class SykmeldingDok(
    val perioder: List<Periode?>?,
    val hovedDiagnose: HovedDiagnose?
)

class GetSykmeldingerDatabaseDevelopment() : GetSykmeldingOpplysningerDatabase {
    override suspend fun getAlleSykmeldinger(fnr: String): List<Sykmelding> {
        logger.info("Henter sykmeldinger fra dev")
        return emptyList()
    }
}

class GetSykmeldingerDatabaseProduction(val database: Database) :
    GetSykmeldingOpplysningerDatabase {

    override suspend fun getAlleSykmeldinger(fnr: String): List<Sykmelding> {
        val sykmeldinger = mutableListOf<Sykmelding>()

        this.database.connection.use { connection ->
            connection.prepareStatement(
                """
                     select * from sykmeldingsopplysninger smo 
                     where smo.pasient_fnr = ?
                    """,
            ).use { statement ->
                statement.setString(1, fnr)
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val sykmelding = resultSet.toSykmelding()
                        sykmeldinger.add(sykmelding)
                    }
                }
            }
        }

        val arbeidsgivere = getArbeidsgivere(sykmeldinger.map { it.sykmeldingId })
        val behandlingsUtfall = getBehandlingsUtfall(sykmeldinger.map { it.sykmeldingId })
        val sykmeldingStatus = getSykmeldingStatus(sykmeldinger.map { it.sykmeldingId })
        val sykmeldingDok = getPerioderAndHovedDiagnose(sykmeldinger.map { it.sykmeldingId })
        return sykmeldinger.map { sykmelding ->
            sykmelding.copy(
                    statusEvent = sykmeldingStatus[sykmelding.sykmeldingId],
                    arbeidsgiver = arbeidsgivere[sykmelding.sykmeldingId],
                    behandlingsUtfall = behandlingsUtfall[sykmelding.sykmeldingId],
                    perioder = sykmeldingDok[sykmelding.sykmeldingId]?.perioder,
                    hovedDiagnose = sykmeldingDok[sykmelding.sykmeldingId]?.hovedDiagnose
            )
        }
    }

    private fun getArbeidsgivere(sykmeldingIds: List<String>): Map<String, Arbeidsgiver> {
        val arbeidsgivere = mutableMapOf<String, Arbeidsgiver>()

        this.database.connection.use { connection ->
            val inClause = sykmeldingIds.joinToString(",") { "?" }
            connection.prepareStatement(
                """
                SELECT * FROM arbeidsgiver arb
                WHERE arb.sykmelding_id IN ($inClause)
            """,
            ).use { statement ->
                sykmeldingIds.forEachIndexed { index, id ->
                    statement.setString(index + 1, id)
                }
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val sykmeldingId = resultSet.getString("sykmelding_id")
                        val arbeidsgiver = resultSet.toArbeidsgiver()
                        arbeidsgivere[sykmeldingId] = arbeidsgiver
                    }
                }
            }
        }

        return arbeidsgivere
    }


    private fun getBehandlingsUtfall(sykmeldingIds: List<String>): Map<String, BehandlingsUtfall?> {

        val behandlingsUtfallList = mutableMapOf<String, BehandlingsUtfall?>()

        this.database.connection.use { connection ->
            val inClause = sykmeldingIds.joinToString(",") { "?" }
            connection.prepareStatement(
                """
                SELECT * FROM behandlingsutfall beh
                WHERE beh.id IN ($inClause)
            """,
            ).use { statement ->
                sykmeldingIds.forEachIndexed { index, id ->
                    statement.setString(index + 1, id)
                }
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val sykmeldingId = resultSet.getString("id")
                        val behandlingsUtfall = resultSet.toBehandlingsutfall()
                        behandlingsUtfallList[sykmeldingId] = behandlingsUtfall
                    }
                }
            }
        }

        return behandlingsUtfallList
    }

    private fun getPerioderAndHovedDiagnose(sykmeldingIds: List<String>): MutableMap<String, SykmeldingDok?> {
        val periodeList = mutableMapOf<String, List<Periode?>?>()
        val hovedDiagnoseList = mutableMapOf<String, HovedDiagnose?>()
        val sykmeldingDokList = mutableMapOf<String, SykmeldingDok?>()
        this.database.connection.use { connection ->
            val inClause = sykmeldingIds.joinToString(",") { "?" }
            connection.prepareStatement(
                """
                     select * from sykmeldingsdokument smd
                     where smd.id IN ($inClause)
                    """,
            ).use { statement ->
                sykmeldingIds.forEachIndexed { index, id ->
                    statement.setString(index + 1, id)
                }
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val sykmeldingId = resultSet.getString("id")
                        val perioder = resultSet.toPerioder()
                        val hovedDiagnose = resultSet.toHovedDiagnose()
                        periodeList[sykmeldingId] = perioder
                        hovedDiagnoseList[sykmeldingId] = hovedDiagnose
                        sykmeldingDokList[sykmeldingId] = SykmeldingDok(perioder, hovedDiagnose)
                    }
                }
            }
        }
        return sykmeldingDokList
    }

    private fun getSykmeldingStatus(sykmeldingIds: List<String?>): Map<String, String?> {
        val statusList = mutableMapOf<String, String?>()

        this.database.connection.use { connection ->
            val inClause = sykmeldingIds.joinToString(",") { "?" }
            connection.prepareStatement(
                """ 
                     select * from sykmeldingstatus status
                     where status.sykmelding_id IN ($inClause)
                    """,
            ).use { statement ->
                sykmeldingIds.forEachIndexed { index, id ->
                    statement.setString(index + 1, id)
                }
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val sykmeldingId = resultSet.getString("sykmelding_id")
                        val status = resultSet.getString("event")
                        statusList[sykmeldingId] = status
                    }
                }
            }
        }

        return statusList
    }

    private fun ResultSet.toSykmelding(): Sykmelding {
        val sykmeldingsopplysninger =
            Sykmelding(
                sykmeldingId = getString("id"),
                mottakId = getString("mottak_id"),
                mottattTidspunkt = getTimestamp("mottatt_tidspunkt").toLocalDateTime(),
                tssId = getString("tss_id"),
                arbeidsgiver = null,
                synligStatus = null,
                behandlingsUtfall = null,
                hovedDiagnose = null,
                merknader = getString("merknader")?.let { objectMapper.readValue<List<Merknad>>(it) },
                // statusEvent = getSykmeldingStatus("id"),
                statusEvent = "APEN",
                perioder = null,
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
        val behandlingsutfallJson = getString("behandlingsutfall")

        return behandlingsutfallJson?.let {
            val behandlingsutfall = objectMapper.readValue<BehandlingsUtfall>(behandlingsutfallJson)
            behandlingsutfall
        }
    }

    private fun ResultSet.toPerioder(): List<Periode?>? {
        val sykmeldingDokumentJson = getString("sykmelding")
        return sykmeldingDokumentJson?.let {
            val sykmeldingDokument =
                objectMapper.readValue<SykmeldingDokument>(sykmeldingDokumentJson)
            sykmeldingDokument.perioder
        }
    }

    private fun ResultSet.toHovedDiagnose(): HovedDiagnose? {
        val sykmeldingDokumentJson = getString("sykmelding")
        return sykmeldingDokumentJson?.let {
            val sykmeldingDokument =
                objectMapper.readValue<SykmeldingDokument>(sykmeldingDokumentJson)
            sykmeldingDokument.medisinskVurdering.hovedDiagnose
        }
    }
}
