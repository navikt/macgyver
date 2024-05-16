package no.nav.syfo.plugins

import io.mockk.mockk
import no.nav.syfo.db.Database
import no.nav.syfo.identendring.update_fnr.UpdateFnrDatabase
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaMessageDTO
import no.nav.syfo.narmesteleder.kafkamodel.NlRequestKafkaMessage
import no.nav.syfo.narmesteleder.kafkamodel.NlResponseKafkaMessage
import no.nav.syfo.sykmelding.aivenmigrering.SykmeldingV2KafkaMessage
import no.nav.syfo.utils.EnvironmentVariables
import org.apache.kafka.clients.producer.KafkaProducer
import org.junit.jupiter.api.Test
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.check.checkModules

class CheckModulesTest : KoinTest {

    val testEnv =
        EnvironmentVariables(
            applicationPort = 69,
            applicationName = "applicationName-test",
            aadAccessTokenV2Url = "aadAccessTokenV2Url-test",
            clientIdV2 = "clientIdV2-test",
            clientSecretV2 = "clientSecretV2-test",
            jwkKeysUrl = "jwkKeysUrl-test",
            jwtIssuer = "jwtIssuer-test",
            oppgavebehandlingUrl = "oppgavebehandlingUrl-test",
            oppgaveScope = "oppgaveScope-test",
            pdlGraphqlPath = "pdlGraphqlPath-test",
            pdlScope = "pdlScope-test",
            narmestelederUrl = "narmestelederUrl-test",
            narmestelederScope = "narmestelederScope-test",
            syfosmregisterDatabaseUsername = "syfosmregisterDatabaseUsername-test",
            syfosmregisterDatabasePassword = "syfosmregisterDatabasePassword-test",
            syfosmregisterDatabaseHost = "syfosmregisterDatabaseHost-test",
            syfosmregisterDatabasePort = "syfosmregisterDatabasePort-test",
            syfosmregisterDatabaseName = "syfosmregisterDatabaseName-test",
            syfosmregisterDatabaseCloudSqlInstance = "syfosmregisterDatabaseCloudSqlInstance-test",
            legeerklaringTopic = "legeerklaringTopic-test",
            safGraphqlPath = "safGraphqlPath-test",
            safScope = "safScope-test",
            dokArkivUrl = "dokArkivUrl-test",
            dokArkivScope = "dokArkivScope-test",
            clusterName = "clusterName-test",
        )

    @Test
    fun verifyKoinApp() {
        koinApplication {
            initProductionModules()

            modules(
                module {
                    // Mock up any "leaf nodes" in the dependency tree that we don't want
                    // instantiated. That way we can verify that all dependencies are satisfied
                    single { testEnv }
                    single { mockk<UpdateFnrDatabase>() }
                    single { mockk<AuthConfiguration>() }
                    single<Database>(named("syfoSmregisterDatabase")) { mockk<Database>() }
                    single<KafkaProducer<String, Any?>>(named("tombstoneProducer")) {
                        mockk<KafkaProducer<String, Any?>>()
                    }
                    single<KafkaProducer<String, SykmeldingV2KafkaMessage?>>(
                        named("kafkaAivenProducer"),
                    ) {
                        mockk<KafkaProducer<String, SykmeldingV2KafkaMessage?>>()
                    }
                    single<KafkaProducer<String, NlResponseKafkaMessage>>(
                        named("nlResponseProducer"),
                    ) {
                        mockk<KafkaProducer<String, NlResponseKafkaMessage>>()
                    }
                    single<KafkaProducer<String, SykmeldingStatusKafkaMessageDTO>>(
                        named("sykmeldingStatusProducer"),
                    ) {
                        mockk<KafkaProducer<String, SykmeldingStatusKafkaMessageDTO>>()
                    }
                    single<KafkaProducer<String, NlRequestKafkaMessage>>(
                        named("nlRequestProducer"),
                    ) {
                        mockk<KafkaProducer<String, NlRequestKafkaMessage>>()
                    }
                },
            )

            checkModules()
        }
    }
}
