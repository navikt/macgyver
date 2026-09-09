package no.nav.syfo.metrics

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.util.pipeline.*
import no.nav.syfo.logging.logger
import no.nav.syfo.logging.teamLogger

fun monitorHttpRequests(developmentMode: Boolean): PipelineInterceptor<Unit, PipelineCall> {
    return {
        val startTime = System.nanoTime()

        try {
            teamLogger().info("Received request: ${call.request.uri}")
            proceed()
        } catch (e: Exception) {
            if (developmentMode) {
                logger.error(
                    "Exception during '${call.request.uri}': ${e.javaClass.simpleName}: ${e.message}",
                    e,
                )
            } else {
                logger.error(
                    "Feil under behandling av HTTP-forespørsel til '${call.request.uri}': ${e.javaClass.simpleName}. Se teamlogs for detaljert exception"
                )
            }
            teamLogger().error(
                "Feil under behandling av HTTP-forespørsel til '${call.request.uri}': ${e.javaClass.simpleName}: ${e.message}. Se exception for detaljer.",
                e,
            )
            throw e
        }
        finally {
            val durationSeconds = (System.nanoTime() - startTime).toDouble() / 1_000_000_000.0
            HTTP_HISTOGRAM.record(durationSeconds)
        }
    }
}
