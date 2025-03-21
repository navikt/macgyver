package no.nav.syfo.metrics

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.util.pipeline.*
import no.nav.syfo.logging.logger
import no.nav.syfo.logging.sikkerlogg

fun monitorHttpRequests(developmentMode: Boolean): PipelineInterceptor<Unit, PipelineCall> {
    return {
        try {
            sikkerlogg.info("Received request: ${call.request.uri}")
            val label = context.request.path()
            val timer = HTTP_HISTOGRAM.labels(label).startTimer()
            proceed()
            timer.observeDuration()
        } catch (e: Exception) {
            if (developmentMode) {
                logger.error(
                    "Exception during '${call.request.uri}': ${e.javaClass.simpleName}: ${e.message}",
                    e,
                )
            } else {
                logger.error(
                    "Feil under behandling av HTTP-forespørsel til '${call.request.uri}': ${e.javaClass.simpleName}. Se securelogs for detaljert exception"
                )
            }
            sikkerlogg.error(
                "Feil under behandling av HTTP-forespørsel til '${call.request.uri}': ${e.javaClass.simpleName}: ${e.message}. Se exception for detaljer.",
                e,
            )
            throw e
        }
    }
}
