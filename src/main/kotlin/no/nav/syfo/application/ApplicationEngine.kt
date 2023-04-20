package no.nav.syfo.application

import com.auth0.jwk.JwkProvider
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.routing
import no.nav.syfo.Environment
import no.nav.syfo.application.api.registerNaisApi
import no.nav.syfo.identendring.UpdateFnrService
import no.nav.syfo.identendring.api.registerFnrApi
import no.nav.syfo.legeerklaering.api.registerDeleteLegeerklaeringApi
import no.nav.syfo.legeerklaering.service.DeleteLegeerklaeringService
import no.nav.syfo.narmesteleder.NarmestelederService
import no.nav.syfo.narmesteleder.api.registrerNarmestelederRequestApi
import no.nav.syfo.oppgave.api.registerHentOppgaverApi
import no.nav.syfo.oppgave.client.OppgaveClient
import no.nav.syfo.service.GjenapneSykmeldingService
import no.nav.syfo.smregistrering.SmregistreringService
import no.nav.syfo.smregistrering.api.registerFerdigstillRegistreringsoppgaveApi
import no.nav.syfo.sykmelding.DeleteSykmeldingService
import no.nav.syfo.sykmelding.api.registerDeleteSykmeldingApi
import no.nav.syfo.sykmelding.api.registerGjenapneSykmeldingApi

fun createApplicationEngine(
    env: Environment,
    applicationState: ApplicationState,
    updateFnrService: UpdateFnrService,
    oppgaveClient: OppgaveClient,
    deleteSykmeldingService: DeleteSykmeldingService,
    gjenapneSykmeldingService: GjenapneSykmeldingService,
    narmestelederService: NarmestelederService,
    jwkProvider: JwkProvider,
    issuer: String,
    deleteLegeerklaeringService: DeleteLegeerklaeringService,
    smregistreringService: SmregistreringService,
): ApplicationEngine =
    embeddedServer(Netty, env.applicationPort) {
        install(ContentNegotiation) {
            jackson {
                registerKotlinModule()
                registerModule(JavaTimeModule())
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
        setupAuth(
            jwkProvider = jwkProvider,
            issuer = issuer,
            clientIdV2 = env.clientIdV2,
        )

        routing {
            registerNaisApi(applicationState)
            swaggerUI(path = "swagger", swaggerFile = "api/oas3/macgyver-api.yaml")

            authenticate("jwt") {
                registerFnrApi(updateFnrService)
                registerGjenapneSykmeldingApi(gjenapneSykmeldingService)
                registerDeleteSykmeldingApi(deleteSykmeldingService)
                registerHentOppgaverApi(oppgaveClient)
                registrerNarmestelederRequestApi(narmestelederService)
                registerDeleteLegeerklaeringApi(deleteLegeerklaeringService)
                registerFerdigstillRegistreringsoppgaveApi(smregistreringService)
            }
        }
    }
