package no.nav.syfo.sykmeldingsopplysninger

import com.fasterxml.jackson.module.kotlin.readValue
import java.sql.ResultSet
import java.time.LocalDate
import no.nav.syfo.db.Database
import no.nav.syfo.db.toList
import no.nav.syfo.model.Merknad
import no.nav.syfo.model.RuleInfo
import no.nav.syfo.utils.objectMapper

interface GetSykmeldingerDatabase {

    fun getAlleSykmeldinger(fnr: String): List<Sykmelding>
}

class GetSykmeldingerDatabaseProduction(val database: Database) : GetSykmeldingerDatabase {

    override fun getAlleSykmeldinger(fnr: String): List<Sykmelding> =
        this.database.connection
            .prepareStatement(
                """
                     select * from sykmeldingopplysninger sm 
                     where sm.fnr = fnr
                    """,
            )
            .use {
                it.setString(1, fnr)
                it.executeQuery().toList { toSykmelding() }
            }

    fun getArbeidsgiver(sykmeldingId: String): Arbeidsgiver =
        this.database.connection
            .prepareStatement(
                """
                     select * from arbeidsgiver arb
                     where arb.sykmelding_id = sykmeldingId
                    """,
            )
            .use {
                it.setString(1, sykmeldingId)
                it.executeQuery().toArbeidsgiver()
            }

    private fun getBehandlingsUtfall(sykmeldingId: String): BehandlingsUtfall? =
        this.database.connection
            .prepareStatement(
                """
                     select * from behandlingsutfall beh
                     where beh.id = sykmeldingId
                    """,
            )
            .use {
                it.setString(1, sykmeldingId)
                it.executeQuery().toBehandlingsutfall()
            }

    private fun getPerioder(sykmeldingId: String): List<Periode>? =
        this.database.connection
            .prepareStatement(
                """
                     select * from sykmeldingsdokument beh
                     where beh.id = sykmeldingId
                    """,
            )
            .use {
                it.setString(1, sykmeldingId)
                it.executeQuery().toPerioder()
            }

    private fun getSykmeldingStatus(sykmeldingId: String): String =
        this.database.connection
            .prepareStatement(
                """
                     select * from sykmeldingstatus status
                     where status.id = sykmeldingId
                    """,
            )
            .use {
                it.setString(1, sykmeldingId)
                it.executeQuery().toString()
            }

    private fun ResultSet.toSykmelding(): Sykmelding {
        val sykmeldingsopplysninger =
            Sykmelding(
                sykmeldingId = "id",
                mottakId = getString("mottak_id"),
                mottattTidspunkt = getTimestamp("mottatt_tidspunkt").toLocalDateTime(),
                tssId = getString("tss_id"),
                arbeidsgiver = getArbeidsgiver("id"),
                synligStatus = null,
                behandlingsUtfall = getBehandlingsUtfall("id"),
                hovedDiagnose =
                    getString("hovedDiagnose")?.let {
                        objectMapper.readValue<HovedDiagnose>(
                            it,
                        )
                    },
                merknader =
                    getString("merknader")?.let { objectMapper.readValue<List<Merknad>>(it) },
                statusEvent = getSykmeldingStatus("id"),
                perioder = getPerioder("id"),
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

        val perioderJson = getString("perioder") ?: return emptyList()
        val perioder: List<Map<String, String>> = objectMapper.readValue(perioderJson)

        return perioder.map {
            Periode(
                fom = LocalDate.parse(it["fom"]),
                tom = LocalDate.parse(it["tom"]),
            )
        }
    }
}
