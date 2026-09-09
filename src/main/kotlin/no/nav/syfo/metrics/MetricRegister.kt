package no.nav.syfo.metrics

import io.micrometer.core.instrument.DistributionSummary
import no.nav.syfo.plugins.appRegistry

const val METRICS_NS = "macgyver"

val HTTP_HISTOGRAM: DistributionSummary =
    DistributionSummary.builder("${METRICS_NS}_requests_duration_seconds")
        .baseUnit("seconds")
        .description("http requests durations for incoming requests in seconds")
        .register(appRegistry)
