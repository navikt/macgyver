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

class DokArkivClient(
    private val url: String,
    private val accessTokenClientV2: AccessTokenClientV2,
    private val scope: String,
    private val httpClient: HttpClient,
) {

    suspend fun feilregistreresJournalpost(
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
                    "feilregistrer av journalpost ok for journalpostid {}, msgId {}, http status {}",
                    journalpostId,
                    sykmeldingId,
                    httpResponse.status.value,
                )
            }
            HttpStatusCode.InternalServerError -> {
                logger.error(
                    "Dokakriv svarte med feilmelding ved feilregistrer av journalpost for sykmeldingId {}",
                    sykmeldingId,
                )
                throw IOException(
                    "Saf svarte med feilmelding ved feilregistrer av journalpost for $journalpostId sykmeldingId $sykmeldingId",
                )
            }
            HttpStatusCode.NotFound -> {
                logger.error(
                    "Journalposten finnes ikke for journalpostid {}, sykmeldingId {}",
                    journalpostId,
                    sykmeldingId,
                )
                throw RuntimeException(
                    "Feilregistrer: Journalposten finnes ikke for journalpostid $journalpostId sykmeldingId $sykmeldingId",
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
                    "Fikk BadRequest ved feilregistrer av journalpostid $journalpostId sykmeldingId $sykmeldingId",
                )
            }
            HttpStatusCode.Unauthorized -> {
                logger.error(
                    "Fikk http status {} for journalpostid {}, sykmeldingId {}",
                    HttpStatusCode.Unauthorized.value,
                    journalpostId,
                    sykmeldingId,
                )
                throw RuntimeException(
                    "Fikk Unauthorized ved feilregistrer av journalpostid $journalpostId sykmeldingId $sykmeldingId",
                )
            }
            HttpStatusCode.Forbidden -> {
                logger.error(
                    "Fikk http status {} for journalpostid {}, sykmeldingId {}",
                    HttpStatusCode.Forbidden.value,
                    journalpostId,
                    sykmeldingId,
                )
                throw RuntimeException(
                    "Fikk Forbidden ved feilregistrer av journalpostid $journalpostId sykmeldingId $sykmeldingId",
                )
            }
            else -> {
                logger.error(
                    "Feil ved feilregistrer av journalpostid {}, sykmeldingId {}. Statuskode: ${httpResponse.status}",
                    journalpostId,
                    sykmeldingId,
                )
                throw RuntimeException(
                    "En ukjent feil oppsto ved feilregistrer av journalpostid $journalpostId. Statuskode: ${httpResponse.status}",
                )
            }
        }
    }
}
