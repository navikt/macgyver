package no.nav.syfo.model

import java.time.LocalDateTime

data class Sykmeldingsopplysninger(
    var id: String,
    val pasientFnr: String,
    val pasientAktoerId: String,
    val legeFnr: String,
    val legeAktoerId: String,
    val mottakId: String,
    val legekontorOrgNr: String?,
    val legekontorHerId: String?,
    val legekontorReshId: String?,
    val epjSystemNavn: String,
    val epjSystemVersjon: String,
    var mottattTidspunkt: LocalDateTime,
    val tssid: String?,
    val merknader: List<Merknad>?,
    val partnerreferanse: String?,
    val utenlandskSykmelding: UtenlandskSykmelding?,
)

data class Sykmeldingsdokument(
    var id: String,
    var sykmelding: Sykmelding,
)
