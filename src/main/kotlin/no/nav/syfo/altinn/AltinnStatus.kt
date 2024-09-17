package no.nav.syfo.altinn

data class AltinnStatus(
    val correspondenceId: String,
    val createdDate: String,
    val orgnummer: String,
    val sendersReference: String,
    val statusChanges: List<StatusChange>,
)

data class StatusChange(
    val date: String,
    val type: String,
)
