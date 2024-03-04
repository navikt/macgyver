package no.nav.syfo.metrics

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.util.pipeline.PipelineContext
import no.nav.syfo.logger
import no.nav.syfo.sikkerlogg

fun monitorHttpRequests(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit {
    return {
        try {
            sikkerlogg.info("Received request: ${call.request.uri}")
            val label = context.request.path()
            val timer = HTTP_HISTOGRAM.labels(label).startTimer()
            proceed()
            timer.observeDuration()
        } catch (e: Exception){
            sikkerlogg.error("Feil under behandling av HTTP-forespørsel til '${call.request.uri}': ${e.javaClass.simpleName}: ${e.message}. Se exception for detaljer.", e)
            throw e
        }
    }
}
