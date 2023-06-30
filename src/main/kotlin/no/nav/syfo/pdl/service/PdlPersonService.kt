package no.nav.syfo.pdl.service

import no.nav.syfo.clients.AccessTokenClientV2
import no.nav.syfo.logger
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.pdl.error.FolkeRegistertIdentNotFoundException
import no.nav.syfo.pdl.model.PdlPerson

class PdlPersonService(
    private val pdlClient: PdlClient,
    private val accessTokenClientV2: AccessTokenClientV2,
    private val pdlScope: String,
) {
    suspend fun getPdlPerson(fnr: String): PdlPerson {
        val token = accessTokenClientV2.getAccessTokenV2(pdlScope)
        val pdlResponse = pdlClient.getPerson(fnr = fnr, token = token)

        if (pdlResponse.errors != null) {
            pdlResponse.errors.forEach { logger.error("PDL kastet error: {} ", it) }
        }
        if (
            pdlResponse.data.hentIdenter == null ||
                pdlResponse.data.hentIdenter.identer.isNullOrEmpty()
        ) {
            logger.error("Fant ikke aktørid i PDL {}")
            throw FolkeRegistertIdentNotFoundException("Fant ikke FolkeRegistertIdent i PDL")
        }
        val pdlNavn = pdlResponse.data.person?.navn?.first()
        if (pdlNavn == null) {
            throw FolkeRegistertIdentNotFoundException(
                "Fant ikke FolkeRegistertIdent med navn i PDL"
            )
        }

        return PdlPerson(
            identer = pdlResponse.data.hentIdenter.identer,
            navn = toFormattedNameString(pdlNavn.fornavn, pdlNavn.mellomnavn, pdlNavn.etternavn)
        )
    }

    suspend fun getFnrs(identer: List<String>, narmesteLederId: String): Map<String, String?> {
        val token = accessTokenClientV2.getAccessTokenV2(pdlScope)
        val pdlResponse = pdlClient.getFnrs(aktorids = identer, token = token)

        if (pdlResponse.errors != null) {
            pdlResponse.errors.forEach {
                logger.error("PDL returnerte error {}, {}", it, narmesteLederId)
            }
        }
        if (
            pdlResponse.data.hentIdenterBolk == null ||
                pdlResponse.data.hentIdenterBolk.isNullOrEmpty()
        ) {
            logger.error("Fant ikke identer i PDL {}", narmesteLederId)
            throw IllegalStateException("Fant ingen identer i PDL, skal ikke kunne skje!")
        }
        pdlResponse.data.hentIdenterBolk.forEach {
            if (it.code != "ok") {
                logger.warn(
                    "Mottok feilkode ${it.code} fra PDL for en eller flere identer, {}",
                    narmesteLederId
                )
            }
        }
        return pdlResponse.data.hentIdenterBolk
            .map {
                it.ident to
                    it.identer?.firstOrNull { ident -> ident.gruppe == "FOLKEREGISTERIDENT" }?.ident
            }
            .toMap()
    }
}

private fun toFormattedNameString(fornavn: String, mellomnavn: String?, etternavn: String): String {
    return if (mellomnavn.isNullOrEmpty()) {
        capitalizeFirstLetter("$fornavn $etternavn")
    } else {
        capitalizeFirstLetter("$fornavn $mellomnavn $etternavn")
    }
}

private fun capitalizeFirstLetter(string: String): String {
    return string
        .lowercase()
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { char -> char.titlecaseChar() } }
        .split("-")
        .joinToString("-") { it.replaceFirstChar { char -> char.titlecaseChar() } }
        .trimEnd()
}
