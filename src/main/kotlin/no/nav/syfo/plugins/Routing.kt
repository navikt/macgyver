package no.nav.syfo.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
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
import no.nav.syfo.narmesteleder.NarmesteLederResponseKafkaProducer
import no.nav.syfo.narmesteleder.NarmestelederService
import no.nav.syfo.narmesteleder.api.registrerNarmestelederApi
import no.nav.syfo.narmesteleder.kafkamodel.NlRequestKafkaMessage
import no.nav.syfo.narmesteleder.kafkamodel.NlResponseKafkaMessage
import no.nav.syfo.oppgave.api.registerHentOppgaverApi
import no.nav.syfo.saf.api.registerJournalpostApi
import no.nav.syfo.sykmelding.DeleteSykmeldingService
import no.nav.syfo.sykmelding.SykmeldingStatusKafkaProducer
import no.nav.syfo.sykmelding.aivenmigrering.SykmeldingV2KafkaMessage
import no.nav.syfo.sykmelding.aivenmigrering.SykmeldingV2KafkaProducer
import no.nav.syfo.sykmelding.api.registerDeleteSykmeldingApi
import no.nav.syfo.utils.EnvironmentVariables
import no.nav.syfo.utils.JacksonKafkaSerializer
import no.nav.syfo.utils.JacksonNullableKafkaSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val env by inject<EnvironmentVariables>()

    val httpClients = HttpClients(env)
    val deps = getRoutingDependencies(env, httpClients)

    routing {
        authenticate("jwt") {
            registerFnrApi(deps.updateFnrService)
            registerDeleteSykmeldingApi(deps.deleteSykmeldingService)
            registerHentOppgaverApi(httpClients.oppgaveClient)
            registrerNarmestelederApi(deps.narmestelederService)
            registerDeleteLegeerklaeringApi(deps.deleteLegeerklaeringService)
            registerJournalpostApi(httpClients.safService)
            getPersonApi(httpClients.pdlService)
        }
    }

    intercept(ApplicationCallPipeline.Monitoring, monitorHttpRequests())
}

class RoutingDeps(
    val updateFnrService: UpdateFnrService,
    val deleteSykmeldingService: DeleteSykmeldingService,
    val narmestelederService: NarmestelederService,
    val deleteLegeerklaeringService: DeleteLegeerklaeringService,
)

/** Intermediate steps between Application.kt refactoring and Koin */
private fun getRoutingDependencies(
    environmentVariables: EnvironmentVariables,
    httpClients: HttpClients
): RoutingDeps {
    val syfosmregisterDatabase =
        Database(
            cloudSqlInstance = environmentVariables.syfosmregisterDatabaseCloudSqlInstance,
            dbHost = environmentVariables.syfosmregisterDatabaseHost,
            dbPort = environmentVariables.syfosmregisterDatabasePort,
            dbName = environmentVariables.syfosmregisterDatabaseName,
            dbUsername = environmentVariables.syfosmregisterDatabaseUsername,
            dbPassword = environmentVariables.syfosmregisterDatabasePassword,
        )

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

    val tombstoneProducer =
        KafkaProducer<String, Any?>(
            KafkaUtils.getAivenKafkaConfig("delete-sykmelding-status-producer")
                .toProducerConfig(
                    "macgyver-tobstone-producer",
                    JacksonNullableKafkaSerializer::class,
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

    val deleteSykmeldingService =
        DeleteSykmeldingService(
            syfosmregisterDatabase,
            statusKafkaProducer,
            tombstoneProducer,
            listOf(
                environmentVariables.manuellTopic,
                environmentVariables.papirSmRegistreringTopic,
            ),
            httpClients.dokArkivClient,
        )

    val narmestelederService =
        NarmestelederService(
            pdlService = httpClients.pdlService,
            kafkaAivenNarmestelederRequestProducer,
            environmentVariables.narmestelederRequestTopic,
            httpClients.narmestelederClient,
        )

    val deleteLegeerklaeringService =
        DeleteLegeerklaeringService(
            tombstoneProducer,
            listOf(environmentVariables.legeerklaringTopic),
        )

    return RoutingDeps(
        updateFnrService = updateFnrService,
        deleteSykmeldingService = deleteSykmeldingService,
        narmestelederService = narmestelederService,
        deleteLegeerklaeringService = deleteLegeerklaeringService,
    )
}
