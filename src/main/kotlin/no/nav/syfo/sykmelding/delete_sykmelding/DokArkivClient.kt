package no.nav.syfo.sykmelding.delete_sykmelding

import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import java.io.IOException
import no.nav.syfo.clients.AccessTokenClientV2
import no.nav.syfo.logging.logger

interface DokArkivClient {
    suspend fun registrerFeilMedJournalpost(
        journalpostId: String,
        sykmeldingId: String,
    )
}

class DokArkivClientProduction(
    private val url: String,
    private val accessTokenClientV2: AccessTokenClientV2,
    private val scope: String,
    private val httpClient: HttpClient,
) : DokArkivClient {

    override suspend fun registrerFeilMedJournalpost(
        journalpostId: String,
        sykmeldingId: String,
    ) {
        val httpResponse =
            httpClient.patch("$url/$journalpostId/feilregistrer/feilregistrerSakstilknytning") {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                val token = accessTokenClientV2.getAccessTokenV2(scope)
                header("Authorization", "Bearer $token")
                header("Nav-Callid", sykmeldingId)
            }
        when (httpResponse.status) {
            HttpStatusCode.OK -> {
                logger.info(
                    "Registrering av journalpost med feil - ok for journalpostid {}, msgId {}, http status {}",
                    journalpostId,
                    sykmeldingId,
                    httpResponse.status.value,
                )
            }
            HttpStatusCode.InternalServerError -> {
                logger.error(
                    "Dokakriv svarte med feilmelding ved forsøk på å registrering av journalpost med feil for sykmeldingId {}",
                    sykmeldingId,
                )
                throw IOException(
                    "Saf svarte med feilmelding ved forsøk på å registrering av journalpost med feil for journalpostId: $journalpostId sykmeldingId: $sykmeldingId",
                )
            }
            HttpStatusCode.NotFound -> {
                logger.error(
                    "Journalposten finnes ikke for journalpostid {}, sykmeldingId {}",
                    journalpostId,
                    sykmeldingId,
                )
                throw RuntimeException(
                    "Feil ved registrering av journalpost: Journalposten finnes ikke for journalpostid $journalpostId sykmeldingId $sykmeldingId",
                )
            }
            HttpStatusCode.BadRequest -> {
                logger.error(
                    "Fikk http status {} for journalpostid {}, sykmeldingId {}",
                    HttpStatusCode.BadRequest.value,
                    journalpostId,
                    sykmeldingId,
                )
                throw RuntimeException(
                    "Fikk BadRequest ved registrering av journalpost med feil med journalpostId: $journalpostId sykmeldingId: $sykmeldingId",
                )
            }
            HttpStatusCode.Unauthorized -> {
                logger.error(
                    "Fikk HTTP status {} for journalpostid {}, sykmeldingId {}",
                    HttpStatusCode.Unauthorized.value,
                    journalpostId,
                    sykmeldingId,
                )
                throw RuntimeException(
                    "Fikk Unauthorized ved registrering av feil journalpost med journalpostid $journalpostId sykmeldingId $sykmeldingId",
                )
            }
            HttpStatusCode.Forbidden -> {
                logger.error(
                    "Fikk HTTP status {} for journalpostid {}, sykmeldingId {}",
                    HttpStatusCode.Forbidden.value,
                    journalpostId,
                    sykmeldingId,
                )
                throw RuntimeException(
                    "Fikk Forbidden ved registrering av feil journalpost med journalpostid $journalpostId sykmeldingId $sykmeldingId",
                )
            }
            else -> {
                logger.error(
                    "Feil ved registrering av feil journalpost med journalpostid {}, sykmeldingId {}. Statuskode: ${httpResponse.status}",
                    journalpostId,
                    sykmeldingId,
                )
                throw RuntimeException(
                    "En ukjent feil oppsto ved registrering av feil journalpost med journalpostid: $journalpostId. Statuskode: ${httpResponse.status}",
                )
            }
        }
    }
}

class DokarkivClientDevelopment() : DokArkivClient {
    override suspend fun registrerFeilMedJournalpost(
        journalpostId: String,
        sykmeldingId: String,
    ) {
        logger.info(
            "ved registrering av feil journalpost med journalpostId: $journalpostId for sykmeldingId: $sykmeldingId"
        )
    }
}
