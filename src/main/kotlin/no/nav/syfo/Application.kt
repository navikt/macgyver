package no.nav.syfo

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.Principal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.routing
import io.prometheus.client.hotspot.DefaultExports
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.collections.listOf
import kotlin.collections.set
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.clients.HttpClients
import no.nav.syfo.db.Database
import no.nav.syfo.identendring.UpdateFnrService
import no.nav.syfo.identendring.api.getPersonApi
import no.nav.syfo.identendring.api.registerFnrApi
import no.nav.syfo.kafka.aiven.KafkaUtils
import no.nav.syfo.kafka.toProducerConfig
import no.nav.syfo.legeerklaering.api.registerDeleteLegeerklaeringApi
import no.nav.syfo.legeerklaering.service.DeleteLegeerklaeringService
import no.nav.syfo.metrics.monitorHttpRequests
import no.nav.syfo.nais.isalive.naisIsAliveRoute
import no.nav.syfo.nais.isready.naisIsReadyRoute
import no.nav.syfo.nais.prometheus.naisPrometheusRoute
import no.nav.syfo.narmesteleder.NarmesteLederResponseKafkaProducer
import no.nav.syfo.narmesteleder.NarmestelederService
import no.nav.syfo.narmesteleder.api.registrerNarmestelederRequestApi
import no.nav.syfo.narmesteleder.kafkamodel.NlRequestKafkaMessage
import no.nav.syfo.narmesteleder.kafkamodel.NlResponseKafkaMessage
import no.nav.syfo.oppgave.api.registerHentOppgaverApi
import no.nav.syfo.oppgave.client.OppgaveClient
import no.nav.syfo.pdl.service.PdlPersonService
import no.nav.syfo.saf.api.registerJournalpostApi
import no.nav.syfo.saf.service.SafService
import no.nav.syfo.sykmelding.DeleteSykmeldingService
import no.nav.syfo.sykmelding.SykmeldingStatusKafkaProducer
import no.nav.syfo.sykmelding.aivenmigrering.SykmeldingV2KafkaMessage
import no.nav.syfo.sykmelding.aivenmigrering.SykmeldingV2KafkaProducer
import no.nav.syfo.sykmelding.api.registerDeleteSykmeldingApi
import no.nav.syfo.utils.JacksonKafkaSerializer
import no.nav.syfo.utils.JacksonNullableKafkaSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val objectMapper: ObjectMapper =
    ObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
        configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
    }

val logger: Logger = LoggerFactory.getLogger("no.nav.syfo.macgyver")
val auditlogg: Logger = LoggerFactory.getLogger("auditLogger")
val sikkerlogg: Logger = LoggerFactory.getLogger("securelog")

fun main() {

    val embeddedServer =
        embeddedServer(
            Netty,
            port = EnvironmentVariables().applicationPort,
            module = Application::module,
        )
    Runtime.getRuntime()
        .addShutdownHook(
            Thread {
                embeddedServer.stop(TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(10))
            },
        )
    embeddedServer.start(true)
}

fun Application.configureRouting(
    applicationState: ApplicationState,
    environmentVariables: EnvironmentVariables,
    jwkProviderAadV2: JwkProvider,
    updateFnrService: UpdateFnrService,
    oppgaveClient: OppgaveClient,
    deleteSykmeldingService: DeleteSykmeldingService,
    narmestelederService: NarmestelederService,
    deleteLegeerklaeringService: DeleteLegeerklaeringService,
    safService: SafService,
    pdlService: PdlPersonService,
) {
    setupAuth(
        jwkProvider = jwkProviderAadV2,
        issuer = environmentVariables.jwtIssuer,
        clientIdV2 = environmentVariables.clientIdV2,
    )

    install(ContentNegotiation) {
        jackson {
            registerKotlinModule()
            registerModule(JavaTimeModule())
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }

    routing {
        naisIsAliveRoute(applicationState)
        naisIsReadyRoute(applicationState)
        naisPrometheusRoute()

        swaggerUI(path = "docs", swaggerFile = "openapi/documentation.yaml")

        authenticate("jwt") {
            registerFnrApi(updateFnrService)
            registerDeleteSykmeldingApi(deleteSykmeldingService)
            registerHentOppgaverApi(oppgaveClient)
            registrerNarmestelederRequestApi(narmestelederService)
            registerDeleteLegeerklaeringApi(deleteLegeerklaeringService)
            registerJournalpostApi(safService)
            getPersonApi(pdlService)
        }
    }

    intercept(ApplicationCallPipeline.Monitoring, monitorHttpRequests())
}

fun Application.setupAuth(
    jwkProvider: JwkProvider,
    issuer: String,
    clientIdV2: String,
) {
    install(Authentication) {
        jwt(name = "jwt") {
            verifier(jwkProvider, issuer)
            validate { credentials ->
                when {
                    hasMacgyverClientIdAudience(credentials, clientIdV2) ->
                        JWTPrincipal(credentials.payload)
                    else -> {
                        unauthorized(credentials)
                    }
                }
            }
        }
    }
}

fun unauthorized(credentials: JWTCredential): Principal? {
    logger.warn(
        "Auth: Unexpected audience for jwt {}, {}",
        StructuredArguments.keyValue("issuer", credentials.payload.issuer),
        StructuredArguments.keyValue("audience", credentials.payload.audience),
    )
    return null
}

fun hasMacgyverClientIdAudience(credentials: JWTCredential, clientIdV2: String): Boolean {
    return credentials.payload.audience.contains(clientIdV2)
}

fun Application.module() {
    val environmentVariables = EnvironmentVariables()
    val applicationState = ApplicationState()

    val jwkProvider =
        JwkProviderBuilder(URL(environmentVariables.jwkKeysUrl))
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()

    val syfosmregisterDatabase =
        Database(
            cloudSqlInstance = environmentVariables.syfosmregisterDatabaseCloudSqlInstance,
            dbHost = environmentVariables.syfosmregisterDatabaseHost,
            dbPort = environmentVariables.syfosmregisterDatabasePort,
            dbName = environmentVariables.syfosmregisterDatabaseName,
            dbUsername = environmentVariables.syfosmregisterDatabaseUsername,
            dbPassword = environmentVariables.syfosmregisterDatabasePassword,
        )

    val smregistreringDatabase =
        Database(
            cloudSqlInstance = environmentVariables.smregisteringDatabaseCloudSqlInstance,
            dbHost = environmentVariables.smregistreringDatabaseHost,
            dbPort = environmentVariables.smregistreringDatabasePort,
            dbName = environmentVariables.smregistreringDatabaseName,
            dbUsername = environmentVariables.smregistreringDatabaseUsername,
            dbPassword = environmentVariables.smregistreringDatabasePassword,
        )

    val httpClients = HttpClients(environmentVariables)

    val kafkaAivenProducer =
        KafkaProducer<String, SykmeldingV2KafkaMessage?>(
            KafkaUtils.getAivenKafkaConfig("sendt-sykmelding-producer")
                .toProducerConfig(
                    "macgyver-producer",
                    JacksonNullableKafkaSerializer::class,
                    StringSerializer::class,
                )
                .apply {
                    this[ProducerConfig.ACKS_CONFIG] = "1"
                    this[ProducerConfig.RETRIES_CONFIG] = 1000
                    this[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] = "false"
                },
        )
    val kafkaAivenNarmestelederRequestProducer =
        KafkaProducer<String, NlRequestKafkaMessage>(
            KafkaUtils.getAivenKafkaConfig("narmesteleder-request-producer")
                .toProducerConfig(
                    "macgyver-producer",
                    JacksonKafkaSerializer::class,
                    StringSerializer::class,
                ),
        )
    val aivenProducerProperties =
        KafkaUtils.getAivenKafkaConfig("sykmelding-status-producer")
            .toProducerConfig(
                environmentVariables.applicationName,
                JacksonKafkaSerializer::class,
                StringSerializer::class,
            )

    val statusKafkaProducer =
        SykmeldingStatusKafkaProducer(
            KafkaProducer(aivenProducerProperties),
            environmentVariables.aivenSykmeldingStatusTopic,
        )

    val sendtSykmeldingKafkaProducerFnr = SykmeldingV2KafkaProducer(kafkaAivenProducer)
    val narmesteLederResponseKafkaProducer =
        NarmesteLederResponseKafkaProducer(
            environmentVariables.nlResponseTopic,
            KafkaProducer<String, NlResponseKafkaMessage>(
                KafkaUtils.getAivenKafkaConfig("narmesteleder-response-producer")
                    .toProducerConfig(
                        "macgyver-producer",
                        JacksonNullableKafkaSerializer::class,
                        StringSerializer::class,
                    ),
            ),
        )

    val updateFnrService =
        UpdateFnrService(
            pdlPersonService = httpClients.pdlService,
            syfoSmRegisterDb = syfosmregisterDatabase,
            sendtSykmeldingKafkaProducer = sendtSykmeldingKafkaProducerFnr,
            narmesteLederResponseKafkaProducer = narmesteLederResponseKafkaProducer,
            narmestelederClient = httpClients.narmestelederClient,
            sendtSykmeldingTopic = environmentVariables.sendSykmeldingV2Topic,
        )

    val tombstoneProducer =
        KafkaProducer<String, Any?>(
            KafkaUtils.getAivenKafkaConfig("delete-sykmelding-status-producer")
                .toProducerConfig(
                    "macgyver-tobstone-producer",
                    JacksonNullableKafkaSerializer::class,
                ),
        )

    val deleteSykmeldingService =
        DeleteSykmeldingService(
            syfosmregisterDatabase,
            statusKafkaProducer,
            tombstoneProducer,
            listOf(
                environmentVariables.manuellTopic,
                environmentVariables.papirSmRegistreringTopic,
            ),
        )

    val narmestelederService =
        NarmestelederService(
            pdlService = httpClients.pdlService,
            kafkaAivenNarmestelederRequestProducer,
            environmentVariables.narmestelederRequestTopic,
        )

    val deleteLegeerklaeringService =
        DeleteLegeerklaeringService(
            tombstoneProducer,
            listOf(environmentVariables.legeerklaringTopic),
        )

    environment.monitor.subscribe(ApplicationStopped) {
        applicationState.ready = false
        applicationState.alive = false
    }

    configureRouting(
        applicationState = applicationState,
        environmentVariables = environmentVariables,
        jwkProviderAadV2 = jwkProvider,
        updateFnrService = updateFnrService,
        oppgaveClient = httpClients.oppgaveClient,
        deleteSykmeldingService = deleteSykmeldingService,
        narmestelederService = narmestelederService,
        deleteLegeerklaeringService = deleteLegeerklaeringService,
        safService = httpClients.safService,
        pdlService = httpClients.pdlService,
    )

    DefaultExports.initialize()
}

data class ApplicationState(
    var alive: Boolean = true,
    var ready: Boolean = true,
)

data class HttpMessage(
    val message: String,
)
