package no.nav.syfo.sykmeldingsopplysninger

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.syfo.model.Behandler
import java.time.LocalDate
import java.time.LocalDateTime
import no.nav.syfo.model.UtenlandskSykmelding

data class Sykmeldingsopplysninger(val fnr: String, val sykmeldinger: List<Sykmelding>)

data class Periode(
    val fom: LocalDate,
    val tom: LocalDate,
)

data class BehandlingsUtfall(
    val status: String,
    @JsonProperty("ruleHits") val ruleHits: List<RuleInfo>?
)

data class RuleInfo(
    val ruleName: String?,
    val messageForSender: String?,
    val messageForUser: String?,
    val ruleStatus: Status
)

data class Sykmelding(
    val sykmeldingId: String,
    val merknader: List<Merknad>?,
    val tssId: String?,
    val statusEvent: SykmeldingStatus?,
    val mottakId: String,
    val mottattTidspunkt: LocalDateTime,
    val behandlingsUtfall: BehandlingsUtfall?,
    val perioder: List<Periode?>?,
    val synligStatus: String?,
    val arbeidsgiver: Arbeidsgiver?,
    val hovedDiagnose: HovedDiagnose?,
    val tidligereArbeidsgiver: Arbeidsgiver?,
    val journalpostId: String?,
    val utenlandskSykmelding: UtenlandskSykmelding? = null,
    val legeHpr: String?,
)

data class SykmeldingDokument(
    val id: String,
    val perioder: List<Periode?>?,
    val medisinskVurdering: MedisinskVurdering,
    val utenlandskSykmelding: UtenlandskSykmelding?
)

data class MedisinskVurdering(
    val hovedDiagnose: HovedDiagnose?,
)

data class Arbeidsgiver(
    val orgnummer: String?,
    val orgNavn: String?,
)

data class HovedDiagnose(
    val kode: String,
    val system: String,
    val tekst: String?,
)

data class Merknad(val type: String, val beskrivelse: String?)

data class SykmeldingStatus(
    val status: String,
    val timestamp: LocalDateTime,
)

enum class Status {
    OK,
    MANUAL_PROCESSING,
    INVALID
}

data class SykmeldingDok(
    val perioder: List<Periode?>?,
    val hovedDiagnose: HovedDiagnose?,
)

data class JournalpostMedPeriode(val journalpostId: String, val periode: Periode?)
