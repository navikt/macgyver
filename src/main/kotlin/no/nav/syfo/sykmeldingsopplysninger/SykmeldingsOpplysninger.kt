package no.nav.syfo.sykmeldingsopplysninger

import java.time.LocalDate
import java.time.LocalDateTime
import no.nav.syfo.model.Merknad
import no.nav.syfo.model.RuleInfo

data class Sykmeldingsopplysninger(val fnr: String, val sykmeldinger: List<Sykmelding>)

data class Periode(
    val fom: LocalDate,
    val tom: LocalDate,
)

data class BehandlingsUtfall(val status: String, val ruleHits: List<RuleInfo>)

data class Sykmelding(
    val sykmeldingId: String,
    val merknader: List<Merknad>?,
    val tssId: String?,
    val statusEvent: String?,
    val mottakId: String,
    val mottattTidspunkt: LocalDateTime,
    val behandlingsUtfall: BehandlingsUtfall?,
    val perioder: List<Periode>,
    val synligStatus: String?,
    val arbeidsgiver: Arbeidsgiver?,
    val hovedDiagnose: HovedDiagnose?,
)

data class Arbeidsgiver(
    val orgnummer: String,
    val orgNavn: String,
)

data class HovedDiagnose(
    val kode: String,
    val system: String,
    val tekst: String?,
)
