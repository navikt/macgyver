package no.nav.syfo.oppgave.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import no.nav.syfo.clients.AccessTokenClientV2
import no.nav.syfo.log
import java.time.LocalDate

class OppgaveClient(
    private val url: String,
    private val accessTokenClientV2: AccessTokenClientV2,
    private val scope: String,
    private val httpClient: HttpClient
) {
    suspend fun hentOppgave(oppgaveId: Int, msgId: String): Oppgave {
        val httpResponse = httpClient.get("$url/$oppgaveId") {
            contentType(ContentType.Application.Json)
            val token = accessTokenClientV2.getAccessTokenV2(scope)
            header("Authorization", "Bearer $token")
            header("X-Correlation-ID", msgId)
        }

        return when (httpResponse.status) {
            HttpStatusCode.OK -> {
                httpResponse.body<Oppgave>()
            }
            else -> {
                val msg = "OppgaveClient hentOppgave kastet feil ${httpResponse.status} ved hentOppgave av oppgave, response: ${httpResponse.body<String>()}"
                throw RuntimeException(msg)
            }
        }
    }

    suspend fun ferdigstillOppgave(ferdigstilloppgave: FerdigstillOppgave, journalpostId: String): Oppgave {
        log.info("Ferdigstiller oppgave for journalpostId $journalpostId")

        val httpResponse = httpClient.patch(url + "/" + ferdigstilloppgave.id) {
            contentType(ContentType.Application.Json)
            val token = accessTokenClientV2.getAccessTokenV2(scope)
            header("Authorization", "Bearer $token")
            header("X-Correlation-ID", journalpostId)
            setBody(ferdigstilloppgave)
        }

        return when (httpResponse.status) {
            HttpStatusCode.OK -> {
                httpResponse.body<Oppgave>()
            }
            else -> {
                val msg = "OppgaveClient ferdigstillOppgave kastet feil ${httpResponse.status} ved ferdigstilling av oppgave, response: ${httpResponse.body<String>()}"
                log.error(msg)
                throw RuntimeException(msg)
            }
        }
    }
}

data class Oppgave(
    val id: Int? = null,
    val versjon: Int? = null,
    val tildeltEnhetsnr: String? = null,
    val opprettetAvEnhetsnr: String? = null,
    val aktoerId: String? = null,
    val journalpostId: String? = null,
    val behandlesAvApplikasjon: String? = null,
    val saksreferanse: String? = null,
    val tilordnetRessurs: String? = null,
    val beskrivelse: String? = null,
    val tema: String? = null,
    val oppgavetype: String,
    val behandlingstype: String? = null,
    val aktivDato: LocalDate,
    val fristFerdigstillelse: LocalDate? = null,
    val prioritet: String,
    val status: String? = null,
    val mappeId: Int? = null
)

data class FerdigstillOppgave(
    val id: Int,
    val versjon: Int,
    val status: String,
    val tilordnetRessurs: String,
    val tildeltEnhetsnr: String,
    val mappeId: Int?,
    val beskrivelse: String? = null
)
