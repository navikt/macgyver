package no.nav.syfo.plugins

import no.nav.syfo.clients.DevelopmentAccessTokenClientV2
import no.nav.syfo.identendring.update_fnr.UpdateFnrDatabase
import no.nav.syfo.identendring.update_fnr.UpdateFnrDatabaseDevelopment
import no.nav.syfo.pdl.PdlPersonService
import no.nav.syfo.pdl.client.DevelopmentPdlClient
import no.nav.syfo.sykmelding.aivenmigrering.SykmeldingV2KafkaProducer
import no.nav.syfo.sykmelding.aivenmigrering.SykmeldingV2KafkaProducerDevelopment
import no.nav.syfo.utils.EnvironmentVariables
import org.koin.core.KoinApplication
import org.koin.dsl.module

fun KoinApplication.initDevelopmentModules() {
    modules(
        developmentEnv,
        developmentPdl,
        developmentKafkaModules,
    )
}

val developmentKafkaModules = module {
    single<UpdateFnrDatabase> { UpdateFnrDatabaseDevelopment() }
    single<SykmeldingV2KafkaProducer> { SykmeldingV2KafkaProducerDevelopment() }
}

val developmentPdl = module {
    single {
        PdlPersonService(
            pdlClient = DevelopmentPdlClient(),
            accessTokenClientV2 = DevelopmentAccessTokenClientV2(),
            pdlScope = "dummy-value",
        )
    }
}

val developmentEnv = module {
    single {
        EnvironmentVariables(
            applicationPort = 8080,
            aadAccessTokenV2Url = "dummy-value",
            clientIdV2 = "dummy-value",
            clientSecretV2 = "dummy-value",
            jwkKeysUrl = "dummy-value",
            jwtIssuer = "dummy-value",
            oppgavebehandlingUrl = "dummy-value",
            oppgaveScope = "dummy-value",
            pdlGraphqlPath = "dummy-value",
            pdlScope = "dummy-value",
            narmestelederScope = "dummy-value",
            syfosmregisterDatabaseUsername = "dummy-value",
            syfosmregisterDatabasePassword = "dummy-value",
            syfosmregisterDatabaseHost = "dummy-value",
            syfosmregisterDatabasePort = "dummy-value",
            syfosmregisterDatabaseName = "dummy-value",
            syfosmregisterDatabaseCloudSqlInstance = "dummy-value",
            safGraphqlPath = "dummy-value",
            safScope = "dummy-value",
            dokArkivUrl = "dummy-value",
            dokArkivScope = "dummy-value",
            clusterName = "dummy-value",
        )
    }
}
