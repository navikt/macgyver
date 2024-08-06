package no.nav.syfo.oppgave

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import java.time.LocalDate
import no.nav.syfo.clients.AccessTokenClientV2
import no.nav.syfo.logging.logger

interface OppgaveClient {
    suspend fun hentOppgave(oppgaveId: Int, msgId: String): Oppgave
}

class ProductionOppgaveClient(
    private val url: String,
    private val accessTokenClientV2: AccessTokenClientV2,
    private val scope: String,
    private val httpClient: HttpClient,
) : OppgaveClient {
    override suspend fun hentOppgave(oppgaveId: Int, msgId: String): Oppgave {
        val httpResponse =
            httpClient.get("$url/$oppgaveId") {
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
                val msg =
                    "OppgaveClient hentOppgave kastet feil ${httpResponse.status} ved hentOppgave av oppgave, response: ${httpResponse.body<String>()}"
                throw RuntimeException(msg)
            }
        }
    }
}

class DevelopmentOppgaveClient : OppgaveClient {
    private val devData: List<Oppgave> =
        listOf(
            Oppgave(
                id = 362848304,
                versjon = 1,
                tildeltEnhetsnr = "0101",
                opprettetAvEnhetsnr = "1111",
                aktoerId = "2779114099843",
                journalpostId = "620048552",
                behandlesAvApplikasjon = "FS22",
                saksreferanse = null,
                tilordnetRessurs = null,
                beskrivelse =
                    "Manuell behandling av sykmelding grunnet følgende regler: Infotrygd returnerte en feil, vi kan ikke automatisk oppdatere Infotrygd",
                tema = "SYM",
                oppgavetype = "BEH_EL_SYM",
                behandlingstype = null,
                aktivDato = LocalDate.parse("2023-09-11"),
                fristFerdigstillelse = LocalDate.parse("2023-09-15"),
                prioritet = "NORM",
                status = "OPPRETTET",
                mappeId = null,
            ),
            Oppgave(
                id = 362851984,
                versjon = 1,
                tildeltEnhetsnr = "0231",
                opprettetAvEnhetsnr = "3333",
                aktoerId = "2843217343728",
                journalpostId = "620048839",
                behandlesAvApplikasjon = "FS22",
                saksreferanse = null,
                tilordnetRessurs = null,
                beskrivelse =
                    "Manuell behandling av sykmelding grunnet følgende regler: Pasienten finnes ikke i Infotrygd",
                tema = "SYM",
                oppgavetype = "BEH_EL_SYM",
                behandlingstype = null,
                aktivDato = LocalDate.parse("2023-09-12"),
                fristFerdigstillelse = LocalDate.parse("2023-09-18"),
                prioritet = "NORM",
                status = "OPPRETTET",
                mappeId = null,
            ),
            Oppgave(
                id = 123,
                versjon = 1,
                tildeltEnhetsnr = "0231",
                opprettetAvEnhetsnr = "3333",
                aktoerId = "2843217343728",
                journalpostId = "620048839",
                behandlesAvApplikasjon = "FS22",
                saksreferanse = null,
                tilordnetRessurs = null,
                beskrivelse =
                    "Manuell behandling av sykmelding grunnet følgende regler: Pasienten finnes ikke i Infotrygd",
                tema = "SYM",
                oppgavetype = "BEH_EL_SYM",
                behandlingstype = null,
                aktivDato = LocalDate.parse("2023-09-12"),
                fristFerdigstillelse = LocalDate.parse("2023-09-18"),
                prioritet = "NORM",
                status = "OPPRETTET",
                mappeId = null,
            ),
        )

    override suspend fun hentOppgave(oppgaveId: Int, msgId: String): Oppgave {
        logger.info("Henter oppgave med id $oppgaveId")

        return devData.find { it.id == oppgaveId }
            ?: throw RuntimeException("Fant ikke oppgave med id $oppgaveId")
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
    val mappeId: Int? = null,
)
