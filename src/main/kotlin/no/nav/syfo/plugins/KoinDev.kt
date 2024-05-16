package no.nav.syfo.plugins

import no.nav.syfo.utils.EnvironmentVariables
import org.koin.core.KoinApplication
import org.koin.dsl.module

fun KoinApplication.initDevelopmentModules() {
    modules(
        developmentEnv,
    )
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
