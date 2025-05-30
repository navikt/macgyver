package no.nav.syfo.sykmeldingsopplysninger

import com.fasterxml.jackson.module.kotlin.readValue
import java.sql.ResultSet
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import no.nav.syfo.db.Database
import no.nav.syfo.db.toList
import no.nav.syfo.utils.objectMapper

interface GetSykmeldingOpplysningerDatabase {
    suspend fun getAlleSykmeldinger(fnr: String): List<Sykmelding>
    suspend fun getFnrForSykmeldingId(sykmeldingId: String): String?
}

class GetSykmeldingerDatabaseProduction(private val database: Database) :
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
            val tidligereArbeidsgiver =
                getTidligereArbeidsgiver(sykmeldinger.map { it.sykmeldingId })
            val behandlingsUtfall = getBehandlingsUtfall(sykmeldinger.map { it.sykmeldingId })
            val sykmeldingStatus = getSykmeldingStatus(sykmeldinger.map { it.sykmeldingId })
            val sykmeldingDok = getPerioderAndHovedDiagnose(sykmeldinger.map { it.sykmeldingId })
            return sykmeldinger.map { sykmelding ->
                sykmelding.copy(
                    statusEvent = sykmeldingStatus[sykmelding.sykmeldingId],
                    arbeidsgiver = arbeidsgivere[sykmelding.sykmeldingId],
                    tidligereArbeidsgiver = tidligereArbeidsgiver[sykmelding.sykmeldingId],
                    behandlingsUtfall = behandlingsUtfall[sykmelding.sykmeldingId],
                    synligStatus =
                        getSynligStatus((behandlingsUtfall[sykmelding.sykmeldingId]?.status)),
                    perioder = sykmeldingDok[sykmelding.sykmeldingId]?.perioder,
                    hovedDiagnose = sykmeldingDok[sykmelding.sykmeldingId]?.hovedDiagnose,
                )
            }
        } else return emptyList()
    }

    override suspend fun getFnrForSykmeldingId(sykmeldingId: String): String? {
        return database.connection.use { conn ->
            conn.prepareStatement("""select pasient_fnr from sykmeldingsopplysninger where id = ?""")
                .use {  ps ->
                    ps.setString(1, sykmeldingId)
                    ps.executeQuery().use { rs ->
                        if(rs.next()) {
                            rs.getString("pasient_fnr")
                        } else {
                            null
                        }
                    }
                }
        }
    }

    private fun getTidligereArbeidsgiver(sykmeldingIds: List<String>): Map<String, Arbeidsgiver> {
        val tidligereArbeidsgivere = mutableMapOf<String, Arbeidsgiver>()
        this.database.connection.use { connection ->
            val inClause = sykmeldingIds.joinToString(",") { "?" }
            connection
                .prepareStatement(
                    """
                SELECT arb.sykmelding_id,
                    arb.tidligere_arbeidsgiver ->> 'orgNavn' AS orgNavn, 
                    arb.tidligere_arbeidsgiver ->> 'orgnummer' AS orgnummer 
                 FROM tidligere_arbeidsgiver arb
                WHERE arb.sykmelding_id IN ($inClause)
            """,
                )
                .use { statement ->
                    sykmeldingIds.forEachIndexed { index, id -> statement.setString(index + 1, id) }
                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            val sykmeldingId = resultSet.getString("sykmelding_id")
                            val arbeidsgiver = resultSet.toTidligereArbeidsgiver()
                            tidligereArbeidsgivere[sykmeldingId] = arbeidsgiver
                        }
                    }
                }
        }
        return tidligereArbeidsgivere
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
                            val sykmelding =
                                resultSet.getString("sykmelding").let {
                                    objectMapper.readValue<SykmeldingDokument>(it)
                                }
                            val perioder = sykmelding.perioder
                            val hovedDiagnose = sykmelding.medisinskVurdering.hovedDiagnose
                            periodeList[sykmeldingId] = perioder
                            hovedDiagnoseList[sykmeldingId] = hovedDiagnose
                            sykmeldingDokList[sykmeldingId] = SykmeldingDok(perioder, hovedDiagnose)
                        }
                    }
                }
        }
        return sykmeldingDokList
    }

    private fun getSykmeldingStatus(sykmeldingIds: List<String?>): Map<String, SykmeldingStatus?> {
        val statusList = mutableMapOf<String, SykmeldingStatus?>()

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
                            val timestamp = resultSet.getTimestamp("timestamp").toLocalDateTime()
                            statusList[sykmeldingId] = SykmeldingStatus(status, timestamp)
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
                tidligereArbeidsgiver = null,
                journalpostId = null,
                utenlandskSykmelding =
                    getString("utenlandsk_sykmelding")?.let { objectMapper.readValue(it) },
            )
        return sykmeldingsopplysninger
    }

    private fun ResultSet.toTidligereArbeidsgiver(): Arbeidsgiver {
        val arbeidsgiver =
            Arbeidsgiver(
                orgnummer = getString("orgnummer"),
                orgNavn = getString("orgNavn"),
            )
        return arbeidsgiver
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
        return when (status) {
            "OK" -> "success"
            "MANUAL_PROCESSING" -> "warning"
            "INVALID" -> "danger"
            else -> "neutral"
        }
    }
}

class GetSykmeldingerDatabaseDevelopment : GetSykmeldingOpplysningerDatabase {

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
                statusEvent = SykmeldingStatus(status = "SENDT", timestamp = LocalDateTime.now()),
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
                            fom = LocalDate.of(2024, 8, 23),
                            tom = LocalDate.of(2024, 8, 29),
                        ),
                    ),
                synligStatus = getSynligStatus(Status.OK),
                arbeidsgiver = Arbeidsgiver("orgnummer", "orgNavn"),
                hovedDiagnose = HovedDiagnose("kode", "system", null),
                tidligereArbeidsgiver = null,
                journalpostId = null,
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
                statusEvent = SykmeldingStatus(status = "SENDT", timestamp = LocalDateTime.now()),
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
                tidligereArbeidsgiver = null,
                journalpostId = null,
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
                statusEvent = SykmeldingStatus(status = "SENDT", timestamp = LocalDateTime.now()),
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
                            fom = LocalDate.of(2024, 8, 23),
                            tom = LocalDate.of(2024, 8, 29),
                        ),
                    ),
                synligStatus = getSynligStatus(Status.OK),
                arbeidsgiver = Arbeidsgiver("orgnummer", "orgNavn"),
                hovedDiagnose = HovedDiagnose("kode", "system", null),
                tidligereArbeidsgiver = null,
                journalpostId = null,
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
                statusEvent = SykmeldingStatus(status = "SENDT", timestamp = LocalDateTime.now()),
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
                            fom = LocalDate.of(2024, 8, 23),
                            tom = LocalDate.of(2024, 8, 29),
                        ),
                    ),
                synligStatus = getSynligStatus(Status.INVALID),
                arbeidsgiver = Arbeidsgiver("orgnummer", "orgNavn"),
                hovedDiagnose = HovedDiagnose("kode", "system", null),
                tidligereArbeidsgiver = null,
                journalpostId = null,
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
                statusEvent =
                    SykmeldingStatus(status = "BEKREFTET", timestamp = LocalDateTime.now()),
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
                            fom = LocalDate.of(2024, 8, 23),
                            tom = LocalDate.of(2024, 8, 29),
                        ),
                    ),
                synligStatus = getSynligStatus(Status.OK),
                arbeidsgiver = null,
                hovedDiagnose = HovedDiagnose("kode", "system", null),
                tidligereArbeidsgiver = Arbeidsgiver("orgnummer", "orgNavn"),
                journalpostId = null,
            ),
        )
    }

    override suspend fun getFnrForSykmeldingId(sykmeldingId: String): String? {
        return null
    }

    private fun getSynligStatus(status: Status): String {
        return when (status) {
            Status.OK -> "success"
            Status.MANUAL_PROCESSING -> "warning"
            Status.INVALID -> "danger"
        }
    }
}
