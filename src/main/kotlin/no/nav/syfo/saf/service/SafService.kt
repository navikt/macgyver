package no.nav.syfo.saf.service

import no.nav.syfo.clients.AccessTokenClientV2
import no.nav.syfo.log
import no.nav.syfo.saf.client.SafClient
import no.nav.syfo.saf.error.JournalposterNotFoundException
import no.nav.syfo.saf.model.Journalposter

class SafService(
    private val safClient: SafClient,
    private val accessTokenClientV2: AccessTokenClientV2,
    private val safScope: String,
) {

    suspend fun getDokumentoversiktBruker(fnr: String): List<Journalposter> {
        val token = accessTokenClientV2.getAccessTokenV2(safScope)
        val getDokumentoversiktBrukerResponse = safClient.getDokumentoversiktBruker(fnr = fnr, token = token)

        if (getDokumentoversiktBrukerResponse.errors != null) {
            getDokumentoversiktBrukerResponse.errors.forEach {
                log.error("SAF kastet error: {} ", it)
            }
        }
        if (getDokumentoversiktBrukerResponse.data.dokumentoversiktBruker.journalposter.isNullOrEmpty()) {
            log.error("Fant ikke journalposter i SAF")
            throw JournalposterNotFoundException("Fant ikke journalposter i SAF")
        }

        return getDokumentoversiktBrukerResponse.data.dokumentoversiktBruker.journalposter
    }
}
