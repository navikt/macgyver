package no.nav.syfo.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import no.nav.syfo.identendring.personApi
import no.nav.syfo.identendring.registerFnrApi
import no.nav.syfo.legeerklaering.registerDeleteLegeerklaeringApi
import no.nav.syfo.metrics.monitorHttpRequests
import no.nav.syfo.narmesteleder.registrerNarmestelederApi
import no.nav.syfo.oppgave.registerHentOppgaverApi
import no.nav.syfo.saf.api.registerJournalpostApi
import no.nav.syfo.sykmelding.delete_sykmelding.registerDeleteSykmeldingApi

fun Application.configureFeatures() {
    routing {
        authenticate("jwt") {
            registerFnrApi()
            registerDeleteSykmeldingApi()
            registerHentOppgaverApi()
            registrerNarmestelederApi()
            registerDeleteLegeerklaeringApi()
            registerJournalpostApi()
            personApi()
        }
    }

    intercept(ApplicationCallPipeline.Monitoring, monitorHttpRequests())
}
