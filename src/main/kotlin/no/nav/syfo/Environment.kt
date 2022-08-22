package no.nav.syfo

data class Environment(
    val applicationPort: Int = getEnvVar("APPLICATION_PORT", "8080").toInt(),
    val applicationName: String = getEnvVar("NAIS_APP_NAME", "macgyver"),
    val aadAccessTokenV2Url: String = getEnvVar("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
    val clientIdV2: String = getEnvVar("AZURE_APP_CLIENT_ID"),
    val clientSecretV2: String = getEnvVar("AZURE_APP_CLIENT_SECRET"),
    val jwtIssuerV2: String = getEnvVar("AZURE_OPENID_CONFIG_ISSUER"),
    val jwkKeysUrlV2: String = getEnvVar("AZURE_OPENID_CONFIG_JWKS_URI"),
    val sendSykmeldingV2Topic: String = "teamsykmelding.syfo-sendt-sykmelding",
    val bekreftSykmeldingV2KafkaTopic: String = "teamsykmelding.syfo-bekreftet-sykmelding",
    val mottattSykmeldingV2Topic: String = "teamsykmelding.syfo-mottatt-sykmelding",
    val aivenEndringsloggTopic: String = "teamsykmelding.macgyver-sykmelding-endringslogg",
    val nlResponseTopic: String = "teamsykmelding.syfo-narmesteleder",
    val aivenSykmeldingStatusTopic: String = "teamsykmelding.sykmeldingstatus-leesah",
    val historiskTopic: String = "teamsykmelding.sykmelding-historisk",
    val legeerklaringTopic: String = "teamsykmelding.legeerklaering",
    val narmestelederRequestTopic: String = "teamsykmelding.syfo-nl-request",
    val papirSmRegistreringTopic: String = "teamsykmelding.papir-sm-registering",
    val manuellTopic: String = "teamsykmelding.sykmelding-manuell",
    val oppgavebehandlingUrl: String = getEnvVar("OPPGAVEBEHANDLING_URL"),
    val oppgaveScope: String = getEnvVar("OPPGAVE_SCOPE"),
    val pdlGraphqlPath: String = getEnvVar("PDL_GRAPHQL_PATH"),
    val pdlScope: String = getEnvVar("PDL_SCOPE"),
    val narmestelederUrl: String = getEnvVar("NARMESTELEDER_URL"),
    val narmestelederScope: String = getEnvVar("NARMESTELEDER_SCOPE"),
    val databaseUsername: String = getEnvVar("DB_MACGYVER_USERNAME"),
    val databasePassword: String = getEnvVar("DB_MACGYVER_PASSWORD"),
    val dbHost: String = getEnvVar("DB_MACGYVER_HOST"),
    val dbPort: String = getEnvVar("DB_MACGYVER_PORT"),
    val dbName: String = getEnvVar("DB_MACGYVER_DATABASE"),
    val pale2registerCloudSqlInstance: String = getEnvVar("PALE_2_REGISTER_CLOUD_SQL_INSTANCE"),
    val syfosmregisteringCloudSqlInstance: String = getEnvVar("SYFOSMREGISTER_CLOUD_SQL_INSTANCE")

) {
    fun jdbcUrl(): String {
        return "jdbc:postgresql://$dbHost:$dbPort/$dbName"
    }
}

fun getEnvVar(varName: String, defaultValue: String? = null) =
    System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")
