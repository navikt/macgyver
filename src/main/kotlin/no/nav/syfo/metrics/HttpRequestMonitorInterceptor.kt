package no.nav.syfo.metrics

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.util.pipeline.PipelineContext
import no.nav.syfo.logger
import no.nav.syfo.sikkerlogg

fun monitorHttpRequests(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit {
    return {
        sikkerlogg.info("Received request: ${call.request.uri}")
        val label = context.request.path()
        val timer = HTTP_HISTOGRAM.labels(label).startTimer()
        proceed()
        timer.observeDuration()
    }
}
