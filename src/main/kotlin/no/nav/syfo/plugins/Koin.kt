package no.nav.syfo.plugins

import io.ktor.server.application.*
import no.nav.syfo.clients.AccessTokenClientV2
import no.nav.syfo.clients.ProductionAccessTokenClientV2
import no.nav.syfo.clients.createHttpClient
import no.nav.syfo.db.Database
import no.nav.syfo.identendring.update_fnr.UpdateFnrDatabase
import no.nav.syfo.identendring.update_fnr.UpdateFnrService
import no.nav.syfo.kafka.aiven.KafkaUtils
import no.nav.syfo.kafka.toProducerConfig
import no.nav.syfo.legeerklaering.DeleteLegeerklaeringService
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaMessageDTO
import no.nav.syfo.narmesteleder.NarmesteLederResponseKafkaProducer
import no.nav.syfo.narmesteleder.NarmestelederClient
import no.nav.syfo.narmesteleder.NarmestelederService
import no.nav.syfo.narmesteleder.kafkamodel.NlRequestKafkaMessage
import no.nav.syfo.narmesteleder.kafkamodel.NlResponseKafkaMessage
import no.nav.syfo.oppgave.OppgaveClient
import no.nav.syfo.pdl.PdlPersonService
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.pdl.client.ProductionPdlClient
import no.nav.syfo.saf.client.SafClient
import no.nav.syfo.saf.service.SafService
import no.nav.syfo.sykmelding.aivenmigrering.SykmeldingV2KafkaMessage
import no.nav.syfo.sykmelding.aivenmigrering.SykmeldingV2KafkaProducer
import no.nav.syfo.sykmelding.delete_sykmelding.DeleteSykmeldingDatabase
import no.nav.syfo.sykmelding.delete_sykmelding.DeleteSykmeldingService
import no.nav.syfo.sykmelding.delete_sykmelding.DokArkivClient
import no.nav.syfo.sykmelding.delete_sykmelding.SykmeldingStatusKafkaProducer
import no.nav.syfo.utils.EnvironmentVariables
import no.nav.syfo.utils.JacksonKafkaSerializer
import no.nav.syfo.utils.JacksonNullableKafkaSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()

        initProductionModules()
        if (environment.developmentMode) {
            initDevelopmentModules()
        }
    }
}

fun KoinApplication.initProductionModules() {
    modules(
        environmentModule,
        applicationStateModule,
        authModule,
        httpClientModule,
        pdlModule,
        kafkaModules,
        oppgaveModule,
        narmestelederModule,
        legeerklaeringModule,
        dokarkivModule,
        sykmeldingModule,
        safModule,
    )
}

val environmentModule = module { single { EnvironmentVariables() } }

val applicationStateModule = module { single { ApplicationState() } }

val authModule = module { single { getProductionAuthConfig(get()) } }

val httpClientModule = module {
    single { createHttpClient() }
    single<AccessTokenClientV2> {
        val env = get<EnvironmentVariables>()

        ProductionAccessTokenClientV2(
            env.aadAccessTokenV2Url,
            env.clientIdV2,
            env.clientSecretV2,
            get(),
        )
    }
}

val pdlModule = module {
    single<PdlClient> {
        ProductionPdlClient(
            httpClient = get(),
            basePath = get<EnvironmentVariables>().pdlGraphqlPath,
            graphQlQuery =
                PdlClient::class
                    .java
                    .getResource("/graphql/getPerson.graphql")!!
                    .readText()
                    .replace(Regex("[\n\t]"), ""),
            graphQlQueryAktorids =
                PdlClient::class
                    .java
                    .getResource("/graphql/getAktorids.graphql")!!
                    .readText()
                    .replace(Regex("[\n\t]"), ""),
        )
    }
    single { PdlPersonService(get(), get(), get<EnvironmentVariables>().pdlScope) }
}

val sykmeldingModule = module {
    single<Database>(named("syfoSmregisterDatabase")) {
        val env = get<EnvironmentVariables>()
        Database(
            cloudSqlInstance = env.syfosmregisterDatabaseCloudSqlInstance,
            dbHost = env.syfosmregisterDatabaseHost,
            dbPort = env.syfosmregisterDatabasePort,
            dbName = env.syfosmregisterDatabaseName,
            dbUsername = env.syfosmregisterDatabaseUsername,
            dbPassword = env.syfosmregisterDatabasePassword,
        )
    }
    single { UpdateFnrDatabase(get(qualifier = named("syfoSmregisterDatabase"))) }
    single { DeleteSykmeldingDatabase(get(qualifier = named("syfoSmregisterDatabase"))) }
    single {
        val env = get<EnvironmentVariables>()

        DeleteSykmeldingService(
            get(),
            get<SykmeldingStatusKafkaProducer>(),
            get(qualifier = named("tombstoneProducer")),
            listOf(
                env.manuellTopic,
                env.papirSmRegistreringTopic,
            ),
            get(),
        )
    }
    single {
        UpdateFnrService(
            get(),
            get(),
            get(),
            get(),
            get(),
            get<EnvironmentVariables>().sendSykmeldingV2Topic,
        )
    }
}

val legeerklaeringModule = module {
    single {
        DeleteLegeerklaeringService(
            get(qualifier = named("tombstoneProducer")),
            listOf(get<EnvironmentVariables>().legeerklaringTopic),
        )
    }
}

val oppgaveModule = module {
    single {
        OppgaveClient(
            get<EnvironmentVariables>().oppgavebehandlingUrl,
            get(),
            get<EnvironmentVariables>().oppgaveScope,
            get(),
        )
    }
}

val narmestelederModule = module {
    single {
        val env = get<EnvironmentVariables>()
        NarmestelederClient(
            get(),
            get(),
            env.narmestelederUrl,
            env.narmestelederScope,
        )
    }
    single {
        NarmestelederService(
            pdlService = get(),
            get(qualifier = named("nlRequestProducer")),
            get<EnvironmentVariables>().narmestelederRequestTopic,
            get(),
        )
    }
}

val safModule = module {
    single {
        SafClient(
            httpClient = get(),
            basePath = get<EnvironmentVariables>().safGraphqlPath,
            graphQlQuery =
                SafClient::class
                    .java
                    .getResource("/graphql/dokumentoversiktBruker.graphql")!!
                    .readText()
                    .replace(Regex("[\n\t]"), ""),
        )
    }
    single {
        SafService(
            get(),
            get(),
            get<EnvironmentVariables>().safScope,
        )
    }
}

val dokarkivModule = module {
    single {
        val env = get<EnvironmentVariables>()

        DokArkivClient(
            env.dokArkivUrl,
            get(),
            env.dokArkivScope,
            get(),
        )
    }
}

val kafkaModules = module {
    single<KafkaProducer<String, SykmeldingV2KafkaMessage?>>(named("kafkaAivenProducer")) {
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
    }
    single<KafkaProducer<String, Any?>>(named("tombstoneProducer")) {
        KafkaProducer<String, Any?>(
            KafkaUtils.getAivenKafkaConfig("delete-sykmelding-status-producer")
                .toProducerConfig(
                    "macgyver-tobstone-producer",
                    JacksonNullableKafkaSerializer::class,
                ),
        )
    }
    single<KafkaProducer<String, NlResponseKafkaMessage>>(named("nlResponseProducer")) {
        KafkaProducer<String, NlResponseKafkaMessage>(
            KafkaUtils.getAivenKafkaConfig("narmesteleder-response-producer")
                .toProducerConfig(
                    "macgyver-producer",
                    JacksonNullableKafkaSerializer::class,
                    StringSerializer::class,
                ),
        )
    }
    single<KafkaProducer<String, SykmeldingStatusKafkaMessageDTO>>(
        named("sykmeldingStatusProducer"),
    ) {
        KafkaProducer<String, SykmeldingStatusKafkaMessageDTO>(
            KafkaUtils.getAivenKafkaConfig("sykmelding-status-producer")
                .toProducerConfig(
                    get<EnvironmentVariables>().applicationName,
                    JacksonKafkaSerializer::class,
                    StringSerializer::class,
                ),
        )
    }
    single<KafkaProducer<String, NlRequestKafkaMessage>>(named("nlRequestProducer")) {
        KafkaProducer<String, NlRequestKafkaMessage>(
            KafkaUtils.getAivenKafkaConfig("narmesteleder-request-producer")
                .toProducerConfig(
                    "macgyver-producer",
                    JacksonKafkaSerializer::class,
                    StringSerializer::class,
                ),
        )
    }
    single { SykmeldingV2KafkaProducer(get(qualifier = named("kafkaAivenProducer"))) }
    single {
        NarmesteLederResponseKafkaProducer(
            get<EnvironmentVariables>().nlResponseTopic,
            get(qualifier = named("nlResponseProducer")),
        )
    }
    single {
        SykmeldingStatusKafkaProducer(
            get(qualifier = named("sykmeldingStatusProducer")),
            get<EnvironmentVariables>().aivenSykmeldingStatusTopic,
        )
    }
}
