package no.nav.syfo.saf.service

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import no.nav.syfo.clients.AccessTokenClientV2
import no.nav.syfo.logging.logger
import no.nav.syfo.saf.client.SafClient
import no.nav.syfo.saf.error.JournalposterNotFoundException
import no.nav.syfo.saf.model.Journalpost
import no.nav.syfo.sykmeldingsopplysninger.JournalpostMedPeriode
import no.nav.syfo.sykmeldingsopplysninger.Periode

interface SafService {
    suspend fun getDokumentoversiktBruker(fnr: String): List<Journalpost>?
    suspend fun getJournalPostsBruker(fnr: String): List<JournalpostMedPeriode>?
}

class SafServiceProduction(
    private val safClient: SafClient,
    private val accessTokenClientV2: AccessTokenClientV2,
    private val safScope: String,
) : SafService {

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

    override suspend fun getJournalPostsBruker(fnr: String): List<JournalpostMedPeriode>? {
        val sykmeldingDokumenter =
            getDokumentoversiktBruker(fnr)?.filter { it.tittel?.contains("Sykmelding") == true }

        return sykmeldingDokumenter?.map {
            JournalpostMedPeriode(
                journalpostId = it.journalpostId,
                periode = parsePeriodeFraTittel(it.tittel)
            )
        }
    }
}

class SafServiceDevelopment() : SafService {
    override suspend fun getJournalPostsBruker(fnr: String): List<JournalpostMedPeriode>? {
        val sykmeldingDokumenter =
            getDokumentoversiktBruker(fnr)?.filter { it.tittel?.contains("Sykmelding") == true }

        return sykmeldingDokumenter?.map {
            JournalpostMedPeriode(
                journalpostId = it.journalpostId,
                periode = parsePeriodeFraTittel(it.tittel)
            )
        }
    }

    override suspend fun getDokumentoversiktBruker(fnr: String): List<Journalpost>? {
        return listOf(Journalpost("671128357", "Avvist sykmelding 26.08.2024 - 15.09.2024"))
    }
}

fun parsePeriodeFraTittel(tittel: String?): Periode? {
    if (tittel == null) return null
    val regex = """(\d{2}\.\d{2}\.\d{4}) - (\d{2}\.\d{2}\.\d{4})""".toRegex()

    val matchResult = regex.find(tittel)
    return if (matchResult != null) {
        val (fomStr, tomStr) = matchResult.destructured
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        val fom = LocalDate.parse(fomStr, formatter)
        val tom = LocalDate.parse(tomStr, formatter)

        Periode(fom, tom)
    } else {
        null
    }
}
