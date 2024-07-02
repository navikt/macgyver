package no.nav.syfo.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import no.nav.syfo.identendring.registerFnrApi
import no.nav.syfo.identendring.registerPersonApi
import no.nav.syfo.legeerklaering.registerDeleteLegeerklaeringApi
import no.nav.syfo.metrics.monitorHttpRequests
import no.nav.syfo.narmesteleder.registrerNarmestelederApi
import no.nav.syfo.oppgave.registerHentOppgaverApi
import no.nav.syfo.saf.api.registerJournalpostApi
import no.nav.syfo.sykmelding.delete_sykmelding.registerDeleteSykmeldingApi
import no.nav.syfo.sykmeldingsopplysninger.registerSykmeldingsOpplysningerApi

fun Application.configureFeatures() {
    routing {
        authenticate(if (application.developmentMode) "local" else "jwt") {
            route("/api") {
                registerFnrApi()
                registerDeleteSykmeldingApi()
                registerHentOppgaverApi()
                registrerNarmestelederApi()
                registerDeleteLegeerklaeringApi()
                registerJournalpostApi()
                registerPersonApi()
                registerSykmeldingsOpplysningerApi()
            }
        }
    }

    intercept(ApplicationCallPipeline.Monitoring, monitorHttpRequests(this.developmentMode))
}
