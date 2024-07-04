package no.nav.syfo.sykmeldingsopplysninger

import com.fasterxml.jackson.module.kotlin.readValue
import java.sql.ResultSet
import java.time.LocalDate
import java.util.*
import no.nav.syfo.db.Database
import no.nav.syfo.utils.objectMapper

interface GetSykmeldingOpplysningerDatabase {
    suspend fun getAlleSykmeldinger(fnr: String): List<Sykmelding>
}

class GetSykmeldingerDatabaseDevelopment() : GetSykmeldingOpplysningerDatabase {

    override suspend fun getAlleSykmeldinger(fnr: String): List<Sykmelding> {
        return listOf(
            Sykmelding(
                sykmeldingId = UUID.randomUUID().toString(),
                merknader =
                listOf(
                    Merknad(
                        type = "Merknadtype",
                        beskrivelse = "beskrivelse",
                    ),
                ),
                tssId = "123456",
                statusEvent = "SENDT",
                mottakId = UUID.randomUUID().toString(),
                mottattTidspunkt = LocalDate.parse("2021-01-01").atStartOfDay(),
                behandlingsUtfall =
                BehandlingsUtfall(
                    status = "OK",
                    ruleHits =
                    listOf(
                        RuleInfo(
                            ruleName = "ruleName",
                            messageForSender = "messageForSender",
                            messageForUser = "messageForUser",
                            ruleStatus = Status.OK,
                        ),
                    ),
                ),
                perioder =
                listOf(
                    Periode(
                        fom = LocalDate.of(2021, 1, 1),
                        tom = LocalDate.of(2021, 1, 30),
                    ),
                ),
                synligStatus = getSynligStatus(Status.OK),
                arbeidsgiver = Arbeidsgiver("orgnummer", "orgNavn"),
                hovedDiagnose = HovedDiagnose("kode", "system", null),
            ),
            Sykmelding(
                sykmeldingId = UUID.randomUUID().toString(),
                merknader =
                listOf(
                    Merknad(
                        type = "Merknadtype",
                        beskrivelse = "beskrivelse",
                    ),
                ),
                tssId = "123456",
                statusEvent = "SENDT",
                mottakId = UUID.randomUUID().toString(),
                mottattTidspunkt = LocalDate.parse("2021-02-10").atStartOfDay(),
                behandlingsUtfall =
                BehandlingsUtfall(
                    status = "MANUAL_PROCESSING",
                    ruleHits =
                    listOf(
                        RuleInfo(
                            ruleName = "ruleName",
                            messageForSender = "messageForSender",
                            messageForUser = "messageForUser",
                            ruleStatus = Status.OK,
                        ),
                    ),
                ),
                perioder =
                listOf(
                    Periode(
                        fom = LocalDate.of(2021, 2, 10),
                        tom = LocalDate.of(2021, 2, 20),
                    ),
                ),
                synligStatus = getSynligStatus(Status.MANUAL_PROCESSING),
                arbeidsgiver = Arbeidsgiver("orgnummer", "orgNavn"),
                hovedDiagnose = HovedDiagnose("kode", "system", null),
            ),
            Sykmelding(
                sykmeldingId = UUID.randomUUID().toString(),
                merknader =
                listOf(
                    Merknad(
                        type = "Merknadtype",
                        beskrivelse = "beskrivelse",
                    ),
                ),
                tssId = "123456",
                statusEvent = "SENDT",
                mottakId = UUID.randomUUID().toString(),
                mottattTidspunkt = LocalDate.parse("2021-02-10").atStartOfDay(),
                behandlingsUtfall =
                BehandlingsUtfall(
                    status = "OK",
                    ruleHits =
                    listOf(
                        RuleInfo(
                            ruleName = "ruleName",
                            messageForSender = "messageForSender",
                            messageForUser = "messageForUser",
                            ruleStatus = Status.OK,
                        ),
                    ),
                ),
                perioder =
                listOf(
                    Periode(
                        fom = LocalDate.of(2021, 2, 21),
                        tom = LocalDate.of(2021, 2, 28),
                    ),
                ),
                synligStatus = getSynligStatus(Status.OK),
                arbeidsgiver = Arbeidsgiver("orgnummer", "orgNavn"),
                hovedDiagnose = HovedDiagnose("kode", "system", null),
            ),
            Sykmelding(
                sykmeldingId = UUID.randomUUID().toString(),
                merknader =
                listOf(
                    Merknad(
                        type = "Merknadtype",
                        beskrivelse = "beskrivelse",
                    ),
                ),
                tssId = "123456",
                statusEvent = "SENDT",
                mottakId = UUID.randomUUID().toString(),
                mottattTidspunkt = LocalDate.parse("2021-01-30").atStartOfDay(),
                behandlingsUtfall =
                BehandlingsUtfall(
                    status = "INVALID",
                    ruleHits =
                    listOf(
                        RuleInfo(
                            ruleName = "ruleName",
                            messageForSender = "messageForSender",
                            messageForUser = "messageForUser",
                            ruleStatus = Status.OK,
                        ),
                    ),
                ),
                perioder =
                listOf(
                    Periode(
                        fom = LocalDate.of(2021, 1, 30),
                        tom = LocalDate.of(2021, 2, 8),
                    ),
                ),
                synligStatus = getSynligStatus(Status.INVALID),
                arbeidsgiver = Arbeidsgiver("orgnummer", "orgNavn"),
                hovedDiagnose = HovedDiagnose("kode", "system", null),
            ),
            Sykmelding(
                sykmeldingId = UUID.randomUUID().toString(),
                merknader =
                listOf(
                    Merknad(
                        type = "Merknadtype",
                        beskrivelse = "beskrivelse",
                    ),
                ),
                tssId = "123456",
                statusEvent = "SENDT",
                mottakId = UUID.randomUUID().toString(),
                mottattTidspunkt = LocalDate.parse("2021-01-15").atStartOfDay(),
                behandlingsUtfall =
                BehandlingsUtfall(
                    status = "OK",
                    ruleHits =
                    listOf(
                        RuleInfo(
                            ruleName = "ruleName",
                            messageForSender = "messageForSender",
                            messageForUser = "messageForUser",
                            ruleStatus = Status.OK,
                        ),
                    ),
                ),
                perioder =
                listOf(
                    Periode(
                        fom = LocalDate.of(2021, 1, 15),
                        tom = LocalDate.of(2021, 2, 3),
                    ),
                ),
                synligStatus = getSynligStatus(Status.OK),
                arbeidsgiver = Arbeidsgiver("orgnummer", "orgNavn"),
                hovedDiagnose = HovedDiagnose("kode", "system", null),
            ),
        )
    }

    private fun getSynligStatus(s: Status): String {
        if (s == Status.OK) {
            return "success"
        } else if (s == Status.MANUAL_PROCESSING) {
            return "warning"
        } else if (s == Status.INVALID) {
            return "danger"
        } else {
            return "neutral"
        }
    }
}

class GetSykmeldingerDatabaseProduction(val database: Database) :
    GetSykmeldingOpplysningerDatabase {

    override suspend fun getAlleSykmeldinger(fnr: String): List<Sykmelding> {
        val sykmeldinger = mutableListOf<Sykmelding>()
        this.database.connection.use { connection ->
            connection
                .prepareStatement(
                    """
                     select * from sykmeldingsopplysninger smo 
                     where smo.pasient_fnr = ?
                    """,
                )
                .use { statement ->
                    statement.setString(1, fnr)
                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            val sykmelding = resultSet.toSykmelding()
                            sykmeldinger.add(sykmelding)
                        }
                    }
                }
        }

        if (sykmeldinger.size > 0) {
            val arbeidsgivere = getArbeidsgivere(sykmeldinger.map { it.sykmeldingId })
            val behandlingsUtfall = getBehandlingsUtfall(sykmeldinger.map { it.sykmeldingId })
            val sykmeldingStatus = getSykmeldingStatus(sykmeldinger.map { it.sykmeldingId })
            val sykmeldingDok = getPerioderAndHovedDiagnose(sykmeldinger.map { it.sykmeldingId })
            return sykmeldinger.map { sykmelding ->
                sykmelding.copy(
                    statusEvent = sykmeldingStatus[sykmelding.sykmeldingId],
                    arbeidsgiver = arbeidsgivere[sykmelding.sykmeldingId],
                    behandlingsUtfall = behandlingsUtfall[sykmelding.sykmeldingId],
                    synligStatus =
                    getSynligStatus((behandlingsUtfall[sykmelding.sykmeldingId]?.status)),
                    perioder = sykmeldingDok[sykmelding.sykmeldingId]?.perioder,
                    hovedDiagnose = sykmeldingDok[sykmelding.sykmeldingId]?.hovedDiagnose,
                )
            }
        } else return emptyList()
    }

    private fun getArbeidsgivere(sykmeldingIds: List<String>): Map<String, Arbeidsgiver> {
        val arbeidsgivere = mutableMapOf<String, Arbeidsgiver>()
        this.database.connection.use { connection ->
            val inClause = sykmeldingIds.joinToString(",") { "?" }
            connection
                .prepareStatement(
                    """
                SELECT * FROM arbeidsgiver arb
                WHERE arb.sykmelding_id IN ($inClause)
            """,
                )
                .use { statement ->
                    sykmeldingIds.forEachIndexed { index, id -> statement.setString(index + 1, id) }
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
            connection
                .prepareStatement(
                    """
                SELECT * FROM behandlingsutfall beh
                WHERE beh.id IN ($inClause)
            """,
                )
                .use { statement ->
                    sykmeldingIds.forEachIndexed { index, id -> statement.setString(index + 1, id) }
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

    private fun getPerioderAndHovedDiagnose(
        sykmeldingIds: List<String>
    ): MutableMap<String, SykmeldingDok?> {
        val periodeList = mutableMapOf<String, List<Periode?>?>()
        val hovedDiagnoseList = mutableMapOf<String, HovedDiagnose?>()
        val sykmeldingDokList = mutableMapOf<String, SykmeldingDok?>()
        this.database.connection.use { connection ->
            val inClause = sykmeldingIds.joinToString(",") { "?" }
            connection
                .prepareStatement(
                    """
                     select * from sykmeldingsdokument smd
                     where smd.id IN ($inClause)
                    """,
                )
                .use { statement ->
                    sykmeldingIds.forEachIndexed { index, id -> statement.setString(index + 1, id) }
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
            connection
                .prepareStatement(
                    """ 
                     select * from sykmeldingstatus status
                     where status.sykmelding_id IN ($inClause)
                    """,
                )
                .use { statement ->
                    sykmeldingIds.forEachIndexed { index, id -> statement.setString(index + 1, id) }
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
                merknader =
                getString("merknader")?.let { objectMapper.readValue<List<Merknad>>(it) },
                // statusEvent = getSykmeldingStatus("id"),
                statusEvent = null,
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

    private fun getSynligStatus(status: String?): String {
        if (status == "OK") {
            return "success"
        } else if (status == "MANUAL_PROCESSING") {
            return "warning"
        } else if (status == "INVALID") {
            return "danger"
        } else {
            return "neutral"
        }
    }
}
