package no.nav.syfo

data class Environment(
    val applicationPort: Int = getEnvVar("APPLICATION_PORT", "8080").toInt(),
    val applicationName: String = getEnvVar("NAIS_APP_NAME", "syfoservice-data-syfosmregister"),
    val aadAccessTokenV2Url: String = getEnvVar("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
    val clientIdV2: String = getEnvVar("AZURE_APP_CLIENT_ID"),
    val clientSecretV2: String = getEnvVar("AZURE_APP_CLIENT_SECRET"),
    val jwtIssuerV2: String = getEnvVar("AZURE_OPENID_CONFIG_ISSUER"),
    val jwkKeysUrlV2: String = getEnvVar("AZURE_OPENID_CONFIG_JWKS_URI"),
    val lastIndexSyfoservice: Int = getEnvVar("LAST_INDEX_SYFOSERVICE").toInt(),
    val lastIndexSyfosmregister: String = getEnvVar("LAST_INDEX_SYFOSMREGISTER"),
    val lastIndexEia: Int = getEnvVar("LAST_INDEX_EIA").toInt(),
    val lastIndexSparenaproxy: String = getEnvVar("LAST_INDEX_SPARENAPROXY"),
    val lastIndexBackup: String = getEnvVar("LAST_INDEX_BACKUP"),
    val lastIndexNlSyfoservice: Int = getEnvVar("LAST_INDEX_NL_SYFOSERVICE").toInt(),
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
    val sykmeldingBucketName: String = getEnvVar("SYKMELDING_BUCKET_NAME"),
    val pale2Bucket: String = getEnvVar("PALE_BUCKET_NAME"),
    val pale2VedleggBucketName: String = getEnvVar("PALE_VEDLEGG_BUCKET_NAME"),
    val oppgavebehandlingUrl: String = getEnvVar("OPPGAVEBEHANDLING_URL"),
    val oppgaveScope: String = getEnvVar("OPPGAVE_SCOPE"),
    val pdlGraphqlPath: String = getEnvVar("PDL_GRAPHQL_PATH"),
    val pdlScope: String = getEnvVar("PDL_SCOPE"),
    val narmestelederUrl: String = getEnvVar("NARMESTELEDER_URL"),
    val narmestelederScope: String = getEnvVar("NARMESTELEDER_SCOPE"),
    val syfosmmanuellDatabaseUrl: String = getEnvVar("SYFOSMMANUELL_BACKEND_DB_URL"),
    val syfosmmanuellDatabaseName: String = "syfosmmanuell-backend",
    val syfosmregisterDatabaseURL: String = getEnvVar("SYFOSMREGISTER_DB_URL"),
    val syfosmregisterDatabaseName: String = getEnvVar("DATABASE_NAME", "smregister"),
    val pale2registerDatabaseURL: String = getEnvVar("PALE_2_REGISTER_DB_URL"),
    val pale2registerDatabaseName: String = getEnvVar("DATABASE_PALE_NAME", "pale-2-register"),
    val sparenaproxyDatabaseURL: String = getEnvVar("SPARENAPROXY_DB_URL"),
    val sparenaproxyDatabaseName: String = getEnvVar("DATABASE_SPARENAPROXY_NAME", "sparenaproxy")
)

data class ServiceUser(
    val serviceuserUsername: String = getEnvVar("SERVICEUSER_USERNAME"),
    val serviceuserPassword: String = getEnvVar("SERVICEUSER_PASSWORD")
)

fun getEnvVar(varName: String, defaultValue: String? = null) =
    System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")
