package no.nav.syfo.sykmeldingsopplysninger

import java.time.LocalDate
import java.time.LocalDateTime
import no.nav.syfo.model.AktivitetIkkeMulig
import no.nav.syfo.model.Gradert
import no.nav.syfo.model.Merknad
import no.nav.syfo.model.RuleInfo

data class Sykmeldingsopplysninger(
    val sykmeldingId: String,
    val fnr: String,
    val merknader: List<Merknad>?,
    val tssId: String?,
    val mottakId: String,
    val mottattTidspunkt: LocalDateTime,
    val behandlingsUtfall: BehandlingsUtfall,
    val perioder: List<Periode>,
    val statusEvent: String,
)

data class Periode(
    val fom: LocalDate,
    val tom: LocalDate,
    val aktivitetIkkeMulig: AktivitetIkkeMulig?,
    val avventendeInnspillTilArbeidsgiver: String?,
    val behandlingsdager: Int?,
    val gradert: Gradert?,
    val reisetilskudd: Boolean
)

data class BehandlingsUtfall(val status: String, val ruleHits: List<RuleInfo>)
