package no.nav.syfo.saf.model

import no.nav.syfo.pdl.client.model.ErrorExtension
import no.nav.syfo.pdl.client.model.ErrorLocation

data class GetDokumentoversiktBrukerResponse(
    val data: ResponseData,
    val errors: List<ResponseError>?,
)

data class ResponseData(
    val dokumentoversiktBruker: DokumentoversiktBruker?,
)

data class DokumentoversiktBruker(
    val journalposter: List<Journalpost>?,
)

data class Journalpost(
    val journalpostId: String,
    val tittel: String?,
)

data class ResponseError(
    val message: String?,
    val locations: List<ErrorLocation>?,
    val path: List<String>?,
    val extensions: ErrorExtension?,
)
