package no.nav.syfo.legeerklaering

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.logging.AuditLogger
import no.nav.syfo.logging.auditlogg
import no.nav.syfo.logging.logger
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.utils.safePrincipal
import org.koin.ktor.ext.inject

fun Route.registerDeleteLegeerklaeringApi() {
    val deleteLegeerklaeringService by inject<DeleteLegeerklaeringService>()

    delete("/api/legeerklaering/{legeerklaeringId}") {
        val legeerklaeringId = call.parameters["legeerklaeringId"]!!

        try {
            val principal = call.safePrincipal()
            auditlogg.info(
                AuditLogger(principal.email)
                    .createcCefMessage(
                        fnr = null,
                        operation = AuditLogger.Operation.WRITE,
                        requestPath = "/api/legeerklaering/$legeerklaeringId",
                        permit = AuditLogger.Permit.PERMIT,
                    ),
            )

            deleteLegeerklaeringService.deleteLegeerklaering(legeerklaeringId)
            logger.info(
                "Sender http OK status tilbake for sletting av legeerklaering med id $legeerklaeringId",
            )
            call.respond(HttpStatusCode.OK, HttpMessage("Vellykket sletting"))
        } catch (e: Exception) {
            logger.error(
                "Kastet exception ved sletting av legeerklaering med id $legeerklaeringId",
                e,
            )
            call.respond(
                HttpStatusCode.InternalServerError,
                HttpMessage("Noe gikk galt ved sletting av legeerklaering, prøv igjen"),
            )
        }
    }
}
