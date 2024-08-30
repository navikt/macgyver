package no.nav.syfo.saf.service

import no.nav.syfo.clients.AccessTokenClientV2
import no.nav.syfo.logging.logger
import no.nav.syfo.saf.client.SafClient
import no.nav.syfo.saf.error.JournalposterNotFoundException
import no.nav.syfo.saf.model.Journalpost

interface SafService {
    suspend fun getDokumentoversiktBruker(fnr: String): List<Journalpost>?
}

class SafServiceProduction(
    private val safClient: SafClient,
    private val accessTokenClientV2: AccessTokenClientV2,
    private val safScope: String,
): SafService {

    override suspend fun getDokumentoversiktBruker(fnr: String): List<Journalpost>? {
        val token = accessTokenClientV2.getAccessTokenV2(safScope)
        val getDokumentoversiktBrukerResponse =
            safClient.getDokumentoversiktBruker(fnr = fnr, token = token)

        if (getDokumentoversiktBrukerResponse.errors != null) {
            getDokumentoversiktBrukerResponse.errors.forEach {
                logger.error("SAF kastet error: {} ", it)
            }
        }
        if (
            getDokumentoversiktBrukerResponse.data.dokumentoversiktBruker != null &&
                getDokumentoversiktBrukerResponse.data.dokumentoversiktBruker.journalposter
                    .isNullOrEmpty()
        ) {
            logger.error("Fant ikke journalposter i SAF")
            throw JournalposterNotFoundException("Fant ikke journalposter i SAF")
        } else {
            return getDokumentoversiktBrukerResponse.data.dokumentoversiktBruker?.journalposter
        }
    }
}

class SafServiceDevelopment(
): SafService {

    override suspend fun getDokumentoversiktBruker(fnr: String): List<Journalpost>? {
        return listOf(Journalpost("12345", "journalpost"))
    }
}
