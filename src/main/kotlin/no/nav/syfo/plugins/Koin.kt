package no.nav.syfo.plugins

import io.ktor.server.application.*
import no.nav.syfo.clients.AccessTokenClientV2
import no.nav.syfo.clients.ProductionAccessTokenClientV2
import no.nav.syfo.clients.createHttpClient
import no.nav.syfo.db.Database
import no.nav.syfo.identendring.update_fnr.UpdateFnrDatabase
import no.nav.syfo.identendring.update_fnr.UpdateFnrDatabaseProduction
import no.nav.syfo.identendring.update_fnr.UpdateFnrService
import no.nav.syfo.kafka.aiven.KafkaUtils
import no.nav.syfo.kafka.toProducerConfig
import no.nav.syfo.legeerklaering.DeleteLegeerklaeringService
import no.nav.syfo.narmesteleder.NarmesteLederRequestKafkaProducer
import no.nav.syfo.narmesteleder.NarmesteLederRequestKafkaProducerProduction
import no.nav.syfo.narmesteleder.NarmesteLederResponseKafkaProducer
import no.nav.syfo.narmesteleder.NarmesteLederResponseKafkaProducerProduction
import no.nav.syfo.narmesteleder.NarmestelederClient
import no.nav.syfo.narmesteleder.NarmestelederService
import no.nav.syfo.narmesteleder.ProductionNarmestelederClient
import no.nav.syfo.oppgave.OppgaveClient
import no.nav.syfo.oppgave.ProductionOppgaveClient
import no.nav.syfo.pdl.PdlPersonService
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.pdl.client.ProductionPdlClient
import no.nav.syfo.saf.client.SafClient
import no.nav.syfo.saf.service.SafService
import no.nav.syfo.sykmelding.aivenmigrering.SykmeldingV2KafkaMessage
import no.nav.syfo.sykmelding.aivenmigrering.SykmeldingV2KafkaProducer
import no.nav.syfo.sykmelding.aivenmigrering.SykmeldingV2KafkaProducerProduction
import no.nav.syfo.sykmelding.delete_sykmelding.DeleteSykmeldingDatabase
import no.nav.syfo.sykmelding.delete_sykmelding.DeleteSykmeldingDatabaseProduction
import no.nav.syfo.sykmelding.delete_sykmelding.DeleteSykmeldingService
import no.nav.syfo.sykmelding.delete_sykmelding.DokArkivClient
import no.nav.syfo.sykmelding.delete_sykmelding.DokArkivClientProduction
import no.nav.syfo.sykmelding.delete_sykmelding.SykmeldingStatusKafkaProducer
import no.nav.syfo.sykmelding.delete_sykmelding.SykmeldingStatusKafkaProducerProduction
import no.nav.syfo.sykmelding.delete_sykmelding.TombstoneKafkaProducer
import no.nav.syfo.sykmelding.delete_sykmelding.TombstoneKafkaProducerProduction
import no.nav.syfo.sykmeldingsopplysninger.GetSykmeldingerDatabase
import no.nav.syfo.sykmeldingsopplysninger.GetSykmeldingerDatabaseProduction
import no.nav.syfo.utils.EnvironmentVariables
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
        sykmeldingsopplysningerModule,
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
            aadAccessTokenUrl = env.aadAccessTokenV2Url,
            clientId = env.clientIdV2,
            clientSecret = env.clientSecretV2,
            httpClient = get(),
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
    single<UpdateFnrDatabase> {
        UpdateFnrDatabaseProduction(get(qualifier = named("syfoSmregisterDatabase")))
    }
    single<DeleteSykmeldingDatabase> {
        DeleteSykmeldingDatabaseProduction(get(qualifier = named("syfoSmregisterDatabase")))
    }
    single<GetSykmeldingerDatabase> {
        GetSykmeldingerDatabaseProduction(get(qualifier = named("syfoSmregisterDatabase")))
    }
    single {
        val env = get<EnvironmentVariables>()

        DeleteSykmeldingService(
            deleteSykmeldingDatabase = get(),
            sykmeldingStatusKafkaProducer = get<SykmeldingStatusKafkaProducer>(),
            tombstoneProducer = get<TombstoneKafkaProducer>(),
            topics =
                listOf(
                    env.manuellTopic,
                    env.papirSmRegistreringTopic,
                ),
            dokArkivClient = get(),
        )
    }
    single {
        UpdateFnrService(
            pdlPersonService = get(),
            updateFnrDatabase = get(),
            sendtSykmeldingKafkaProducer = get(),
            narmesteLederResponseKafkaProducer = get(),
            narmestelederClient = get(),
            sendtSykmeldingTopic = get<EnvironmentVariables>().sendSykmeldingV2Topic,
        )
    }
}

val legeerklaeringModule = module {
    single {
        DeleteLegeerklaeringService(
            tombstoneProducer = get(),
            topics = listOf(get<EnvironmentVariables>().legeerklaringTopic),
        )
    }
}

val oppgaveModule = module {
    single<OppgaveClient> {
        ProductionOppgaveClient(
            url = get<EnvironmentVariables>().oppgavebehandlingUrl,
            accessTokenClientV2 = get(),
            scope = get<EnvironmentVariables>().oppgaveScope,
            httpClient = get(),
        )
    }
}

val narmestelederModule = module {
    single<NarmestelederClient> {
        val env = get<EnvironmentVariables>()
        ProductionNarmestelederClient(
            httpClient = get(),
            accessTokenClientV2 = get(),
            baseUrl = env.narmestelederUrl,
            resource = env.narmestelederScope,
        )
    }
    single {
        NarmestelederService(
            pdlService = get(),
            narmestelederRequestProducer = get(),
            narmestelederClient = get(),
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
            safClient = get(),
            accessTokenClientV2 = get(),
            safScope = get<EnvironmentVariables>().safScope,
        )
    }
}

val dokarkivModule = module {
    single<DokArkivClient> {
        val env = get<EnvironmentVariables>()

        DokArkivClientProduction(
            url = env.dokArkivUrl,
            accessTokenClientV2 = get(),
            scope = env.dokArkivScope,
            httpClient = get(),
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
    single<TombstoneKafkaProducer> { TombstoneKafkaProducerProduction() }

    single<NarmesteLederRequestKafkaProducer> {
        NarmesteLederRequestKafkaProducerProduction(
            get<EnvironmentVariables>().narmestelederRequestTopic
        )
    }
    single<NarmesteLederResponseKafkaProducer> {
        NarmesteLederResponseKafkaProducerProduction(
            get<EnvironmentVariables>().nlResponseTopic,
        )
    }
    single<SykmeldingV2KafkaProducer> {
        SykmeldingV2KafkaProducerProduction(get(qualifier = named("kafkaAivenProducer")))
    }
    single<SykmeldingStatusKafkaProducer> {
        SykmeldingStatusKafkaProducerProduction(
            get<EnvironmentVariables>().applicationName,
            get<EnvironmentVariables>().aivenSykmeldingStatusTopic,
        )
    }
}

val sykmeldingsopplysningerModule = module {
    single<GetSykmeldingerDatabase> {
        GetSykmeldingerDatabaseProduction(get(qualifier = named("syfoSmregisterDatabase")))
    }
}
