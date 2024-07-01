package no.nav.syfo.plugins

import no.nav.syfo.clients.DevelopmentAccessTokenClientV2
import no.nav.syfo.identendring.update_fnr.UpdateFnrDatabase
import no.nav.syfo.identendring.update_fnr.UpdateFnrDatabaseDevelopment
import no.nav.syfo.identendring.update_fnr.UpdateFnrService
import no.nav.syfo.narmesteleder.DevelopmentNarmestelederClient
import no.nav.syfo.narmesteleder.NarmesteLederRequestKafkaProducer
import no.nav.syfo.narmesteleder.NarmesteLederRequestKafkaProducerDevelopment
import no.nav.syfo.narmesteleder.NarmestelederClient
import no.nav.syfo.narmesteleder.NarmestelederService
import no.nav.syfo.oppgave.DevelopmentOppgaveClient
import no.nav.syfo.oppgave.OppgaveClient
import no.nav.syfo.pdl.PdlPersonService
import no.nav.syfo.pdl.client.DevelopmentPdlClient
import no.nav.syfo.sykmelding.aivenmigrering.SykmeldingV2KafkaProducer
import no.nav.syfo.sykmelding.aivenmigrering.SykmeldingV2KafkaProducerDevelopment
import no.nav.syfo.sykmelding.delete_sykmelding.DeleteSykmeldingDatabase
import no.nav.syfo.sykmelding.delete_sykmelding.DeleteSykmeldingDatabaseDevelopment
import no.nav.syfo.sykmelding.delete_sykmelding.DeleteSykmeldingService
import no.nav.syfo.sykmelding.delete_sykmelding.DokArkivClient
import no.nav.syfo.sykmelding.delete_sykmelding.DokarkivClientDevelopment
import no.nav.syfo.sykmelding.delete_sykmelding.SykmeldingStatusKafkaProducer
import no.nav.syfo.sykmelding.delete_sykmelding.SykmeldingStatusKafkaProducerDevelopment
import no.nav.syfo.sykmelding.delete_sykmelding.TombstoneKafkaProducer
import no.nav.syfo.sykmelding.delete_sykmelding.TombstoneKafkaProducerDevelopment
import no.nav.syfo.sykmeldingsopplysninger.DevelopmentSykmeldingsOpplysningerClient
import no.nav.syfo.sykmeldingsopplysninger.GetSykmeldingerDatabase
import no.nav.syfo.sykmeldingsopplysninger.GetSykmeldingerDatabaseDevelopment
import no.nav.syfo.sykmeldingsopplysninger.SykmeldingsOpplysningerClient
import no.nav.syfo.utils.EnvironmentVariables
import org.koin.core.KoinApplication
import org.koin.dsl.module

fun KoinApplication.initDevelopmentModules() {
    modules(
        developmentEnv,
        developmentPdl,
        developmentOppgaveModule,
        developmentKafkaModules,
        developmentNarmestelederModule,
        developmentSykmeldingModule,
        developmentDokarkivModule,
        developmentSykmeldingsopplysningerModule,
    )
}

val developmentKafkaModules = module {
    single<UpdateFnrDatabase> { UpdateFnrDatabaseDevelopment() }
    single<SykmeldingV2KafkaProducer> { SykmeldingV2KafkaProducerDevelopment() }
    single<NarmesteLederRequestKafkaProducer> { NarmesteLederRequestKafkaProducerDevelopment() }
    single<SykmeldingStatusKafkaProducer> { SykmeldingStatusKafkaProducerDevelopment() }
    single<TombstoneKafkaProducer> { TombstoneKafkaProducerDevelopment() }
}

val developmentOppgaveModule = module { single<OppgaveClient> { DevelopmentOppgaveClient() } }

val developmentNarmestelederModule = module {
    single<NarmestelederClient> { DevelopmentNarmestelederClient() }
    single {
        NarmestelederService(
            pdlService = get(),
            narmestelederRequestProducer = get(),
            narmestelederClient = get(),
        )
    }
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

val developmentSykmeldingModule = module {
    single<UpdateFnrDatabase> { UpdateFnrDatabaseDevelopment() }
    single<DeleteSykmeldingDatabase> { DeleteSykmeldingDatabaseDevelopment() }
    single<GetSykmeldingerDatabase> { GetSykmeldingerDatabaseDevelopment() }
    single {
        val env = get<EnvironmentVariables>()

        DeleteSykmeldingService(
            deleteSykmeldingDatabase = get(),
            sykmeldingStatusKafkaProducer = get(),
            tombstoneProducer = get(),
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

val developmentDokarkivModule = module { single<DokArkivClient> { DokarkivClientDevelopment() } }

val developmentSykmeldingsopplysningerModule = module {
    single<SykmeldingsOpplysningerClient> { DevelopmentSykmeldingsOpplysningerClient() }
}
