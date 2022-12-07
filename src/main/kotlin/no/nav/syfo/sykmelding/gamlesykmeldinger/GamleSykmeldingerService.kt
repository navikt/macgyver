package no.nav.syfo.sykmelding.gamlesykmeldinger

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.db.Database
import no.nav.syfo.log
import no.nav.syfo.sykmelding.gamlesykmeldinger.db.getSykmelding
import no.nav.syfo.sykmelding.gamlesykmeldinger.kafka.GamleSykmeldingerKafkaProducer
import org.apache.kafka.clients.consumer.KafkaConsumer
import kotlin.time.Duration.Companion.seconds

class GamleSykmeldingerService(
    private val kafkaConsumer: KafkaConsumer<String, String>,
    private val topic: String,
    private val db: Database,
    private val gamleSykmeldingerKafkaProducer: GamleSykmeldingerKafkaProducer,
    private val applicationState: ApplicationState

) {

    private var sykmeldingCount = 0
    @OptIn(DelicateCoroutinesApi::class)
    fun consume() {
        GlobalScope.launch(Dispatchers.IO) {
            while (applicationState.ready) {
                log.info("totalt $sykmeldingCount")
                delay(30.seconds)
            }
        }
        GlobalScope.launch(Dispatchers.IO) {
            log.info("Starting job")
            while (applicationState.ready) {
                kafkaConsumer.subscribe(listOf(topic))
                try {
                    getSykmeldingFromDbAndWriteToKafka()
                } catch (e: Exception) {
                    log.error("Noe gikk galt", e)
                    throw e
                }
            }
        }
    }

    private fun getSykmeldingFromDbAndWriteToKafka() {
        while (applicationState.ready) {
            val records = kafkaConsumer.poll(java.time.Duration.ZERO)
            records.forEach {
                sykmeldingCount++
                val receivedSykmeldingMedBehandlingsutfall = db.getSykmelding(it.value())
                if (receivedSykmeldingMedBehandlingsutfall != null) {
                    gamleSykmeldingerKafkaProducer.send(it.value(), receivedSykmeldingMedBehandlingsutfall)
                }
            }
        }
    }
}
