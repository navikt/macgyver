package no.nav.syfo.saf.model

data class GetDokumentoversiktBrukerRequest(val query: String, val variables: Variables)

data class Variables(
    val brukerId: BrukerIdInput,
    val foerste: Int,
    )