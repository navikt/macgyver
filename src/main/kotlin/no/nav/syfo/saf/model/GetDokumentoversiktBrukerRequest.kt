package no.nav.syfo.saf.model

data class GetDokumentoversiktBrukerRequest(val query: String, val variables: Variables)

data class Variables(
    val id: String,
    val type: String,
    val foerste: Int,
    val tema: List<String> = listOf("SYM")
)
