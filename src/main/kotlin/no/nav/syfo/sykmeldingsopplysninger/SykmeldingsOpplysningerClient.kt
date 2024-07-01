package no.nav.syfo.sykmeldingsopplysninger

import java.time.LocalDate
import java.util.UUID
import no.nav.syfo.model.Merknad
import no.nav.syfo.model.RuleInfo
import no.nav.syfo.model.Status

interface SykmeldingsOpplysningerClient {
    suspend fun getSykmeldingsopplysninger(fnr: String): Sykmeldingsopplysninger
}

class ProductionSykmeldingsOpplysningerClient(
    private val getSykmeldingerDatabase: GetSykmeldingerDatabase
) : SykmeldingsOpplysningerClient {
    override suspend fun getSykmeldingsopplysninger(fnr: String): Sykmeldingsopplysninger {
        return Sykmeldingsopplysninger(fnr, getSykmeldingerDatabase.getAlleSykmeldinger(fnr))
    }
}

class DevelopmentSykmeldingsOpplysningerClient() : SykmeldingsOpplysningerClient {
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

    override suspend fun getSykmeldingsopplysninger(fnr: String): Sykmeldingsopplysninger {
        return Sykmeldingsopplysninger(
            fnr = fnr,
            sykmeldinger =
                listOf(
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
                ),
        )
    }
}
