package no.nav.syfo.model

data class ArbeidsgiverStatus(
    val sykmeldingId: String,
    val orgnummer: String,
    val juridiskOrgnummer: String?,
    val orgnavn: String
)

enum class StatusEvent {
    APEN, AVBRUTT, UTGATT, SENDT, BEKREFTET, SLETTET
}
