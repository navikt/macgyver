package no.nav.syfo

data class Environment(
    val applicationPort: Int = getEnvVar("APPLICATION_PORT", "8080").toInt(),
    val applicationName: String = getEnvVar("NAIS_APP_NAME", "macgyver"),
    val aadAccessTokenV2Url: String = getEnvVar("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
    val clientIdV2: String = getEnvVar("AZURE_APP_CLIENT_ID"),
    val clientSecretV2: String = getEnvVar("AZURE_APP_CLIENT_SECRET"),
    val jwkKeysUrl: String = getEnvVar("AZURE_OPENID_CONFIG_JWKS_URI"),
    val jwtIssuer: String = getEnvVar("AZURE_OPENID_CONFIG_ISSUER"),
    val sendSykmeldingV2Topic: String = "teamsykmelding.syfo-sendt-sykmelding",
    val bekreftSykmeldingV2KafkaTopic: String = "teamsykmelding.syfo-bekreftet-sykmelding",
    val mottattSykmeldingV2Topic: String = "teamsykmelding.syfo-mottatt-sykmelding",
    val aivenEndringsloggTopic: String = "teamsykmelding.macgyver-sykmelding-endringslogg",
    val nlResponseTopic: String = "teamsykmelding.syfo-narmesteleder",
    val aivenSykmeldingStatusTopic: String = "teamsykmelding.sykmeldingstatus-leesah",
    val narmestelederRequestTopic: String = "teamsykmelding.syfo-nl-request",
    val papirSmRegistreringTopic: String = "teamsykmelding.papir-sm-registering",
    val manuellTopic: String = "teamsykmelding.sykmelding-manuell",
    val oppgavebehandlingUrl: String = getEnvVar("OPPGAVEBEHANDLING_URL"),
    val oppgaveScope: String = getEnvVar("OPPGAVE_SCOPE"),
    val pdlGraphqlPath: String = getEnvVar("PDL_GRAPHQL_PATH"),
    val pdlScope: String = getEnvVar("PDL_SCOPE"),
    val narmestelederUrl: String = getEnvVar("NARMESTELEDER_URL"),
    val narmestelederScope: String = getEnvVar("NARMESTELEDER_SCOPE"),
    val syfosmregisterDatabaseUsername: String = getEnvVar("DB_MACGYVER_USERNAME"),
    val syfosmregisterDatabasePassword: String = getEnvVar("DB_MACGYVER_PASSWORD"),
    val syfosmregisterDatabaseHost: String = getEnvVar("DB_MACGYVER_HOST"),
    val syfosmregisterDatabasePort: String = getEnvVar("DB_MACGYVER_PORT"),
    val syfosmregisterDatabaseName: String = getEnvVar("SYFOSMREGISTER_DB_NAME"),
    val syfosmregisterDatabaseCloudSqlInstance: String = getEnvVar("SYFOSMREGISTER_CLOUD_SQL_INSTANCE"),
    val legeerklaringTopic: String = "teamsykmelding.legeerklaering",
    val smregistreringDatabaseUsername: String = getEnvVar("NAIS_DATABASE_MACGYVER_USERNAME"),
    val smregistreringDatabasePassword: String = getEnvVar("NAIS_DATABASE_MACGYVER_PASSWORD"),
    val smregistreringDatabaseHost: String = getEnvVar("NAIS_DATABASE_MACGYVER_HOST"),
    val smregistreringDatabasePort: String = getEnvVar("NAIS_DATABASE_MACGYVER_PORT"),
    val smregistreringDatabaseName: String = getEnvVar("SMREGISTRERING_DB_NAME"),
    val smregisteringDatabaseCloudSqlInstance: String = getEnvVar("SMREGISTRERING_CLOUD_SQL_INSTANCE")

)
fun getEnvVar(varName: String, defaultValue: String? = null) =
    System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")
