package no.nav.syfo.identendring.model

import java.time.LocalDate
import java.time.Month
import no.nav.syfo.identendring.db.Periode

typealias MedisinskVurderingDB = no.nav.syfo.identendring.db.MedisinskVurdering

private val diagnoserSomGirRedusertArbgiverPeriode = listOf("R991", "U071", "U072", "A23", "R992")
val koronaForsteFraDato = LocalDate.of(2020, Month.MARCH, 15)
val koronaForsteTilDato = LocalDate.of(2021, Month.OCTOBER, 1)
val koronaAndreFraDato = LocalDate.of(2021, Month.NOVEMBER, 30)
val koronaAndreTilDato = LocalDate.of(2022, Month.JULY, 1)

fun MedisinskVurderingDB.getHarRedusertArbeidsgiverperiode(
    sykmeldingsperioder: List<Periode>
): Boolean {
    val sykmeldingsperioderInnenforKoronaregler =
        sykmeldingsperioder.filter { periodeErInnenforKoronaregler(it.fom, it.tom) }
    if (sykmeldingsperioderInnenforKoronaregler.isEmpty()) {
        return false
    }
    if (
        hovedDiagnose != null && diagnoserSomGirRedusertArbgiverPeriode.contains(hovedDiagnose.kode)
    ) {
        return true
    } else if (
        !biDiagnoser.isNullOrEmpty() &&
            biDiagnoser.find { diagnoserSomGirRedusertArbgiverPeriode.contains(it.kode) } != null
    ) {
        return true
    }
    return checkSmittefare()
}

private fun MedisinskVurderingDB.checkSmittefare() =
    annenFraversArsak?.grunn?.any { annenFraverGrunn ->
        annenFraverGrunn == no.nav.syfo.identendring.db.AnnenFraverGrunn.SMITTEFARE
    } == true

fun periodeErInnenforKoronaregler(fom: LocalDate, tom: LocalDate): Boolean {
    return (fom.isAfter(koronaAndreFraDato) && fom.isBefore(koronaAndreTilDato)) ||
        (fom.isBefore(koronaForsteTilDato) && tom.isAfter(koronaForsteFraDato))
}
