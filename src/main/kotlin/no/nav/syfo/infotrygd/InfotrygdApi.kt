package no.nav.syfo.infotrygd

import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.syfo.logging.logger
import no.nav.syfo.model.Diagnose
import org.koin.ktor.ext.inject
import java.util.UUID

data class InfotrygdGetRequest(
    val ident: String,
    val tkNummer: String?,
    val hoveddiagnose: String?,
    val hoveddiagnoseKodeSystem: String?,
    val bidiagnose: String?,
    val bidiagnoseKodeSystem: String?,
    val identBehandler: String?,
)

data class InfotrygdGetResponse(
    val identDato: String?,
    val tkNummer: String?,
    val traceId: String,
)

data class InfotrygdDetailedResponse(
    val response: String,
)

fun InfotrygdGetRequest.toInfotrygdQuery(): InfotrygdQuery {
    return InfotrygdQuery(
        ident = ident,
        tknumber = tkNummer,
        hoveddiagnose = hoveddiagnose?.let {
            Diagnose(
                this.hoveddiagnoseKodeSystem
                    ?: throw IllegalArgumentException("hoveddiagnoseKodeSystem must not be null"),
                this.hoveddiagnose,
                null,
            )
        },
        bidiagnose = bidiagnose?.let {
            Diagnose(
                this.bidiagnoseKodeSystem
                    ?: throw IllegalArgumentException("bidiagnoseKodeSystem must not be null"),
                this.bidiagnose,
                null,
            )
        },
        fodselsnrBehandler = identBehandler,
        traceId = UUID.randomUUID().toString(),
        fom = null,
        tom = null,
    )
}

fun Route.registerInfotrygdApi() {
    val infotrygdService by inject<InfotrygdService>()
    post("/infotrygd") {
        logger.info("got infotrygd request")
        val request = call.receive<InfotrygdGetRequest>()
        val infotrygdQuery = request.toInfotrygdQuery()
        try {
            logger.info("traceId: ${infotrygdQuery.traceId}")
            val response = infotrygdService.getInfotrygdResponse(infotrygdQuery)

            call.respond(
                InfotrygdGetResponse(
                    tkNummer = response.tkNummer,
                    identDato = response.identDato,
                    traceId = infotrygdQuery.traceId,
                ),
            );
        } catch (
            ex: Exception
        ) {
            logger.error("error in infotrygd request: {}", ex.message)
            call.respond(
                InfotrygdGetResponse(
                    tkNummer = "ERROR",
                    identDato = "ERROR",
                    traceId = infotrygdQuery.traceId,
                ),
            )
        }


    }

    post("/infotrygd/detailed") {
        logger.info("got infotrygd request")
        val request = call.receive<InfotrygdGetRequest>()
        val infotrygdQuery = request.toInfotrygdQuery()
        try {
            logger.info("traceId: ${infotrygdQuery.traceId}")
            val response = infotrygdService.getDetailedInfotrygdResponse(infotrygdQuery)

            call.respond(
                InfotrygdDetailedResponse(
                    response = response.toPrettyString(),
                )
            );
        } catch (
            ex: Exception
        ) {
            logger.error("error in infotrygd request: {}", ex.message)
            call.respond(
                InfotrygdGetResponse(
                    tkNummer = "ERROR",
                    identDato = "ERROR",
                    traceId = infotrygdQuery.traceId,
                ),
            )
        }

    }
}
