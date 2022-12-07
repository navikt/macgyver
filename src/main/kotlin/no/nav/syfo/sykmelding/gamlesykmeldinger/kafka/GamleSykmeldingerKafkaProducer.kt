package no.nav.syfo.sykmelding.gamlesykmeldinger.kafka

import no.nav.syfo.log
import no.nav.syfo.sykmelding.gamlesykmeldinger.db.model.ReceivedSykmeldingMedBehandlingsutfall
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

class GamleSykmeldingerKafkaProducer(
    private val kafkaProducer: KafkaProducer<String, ReceivedSykmeldingMedBehandlingsutfall>,
    private val topic: String
) {
    fun send(sykmeldingId: String, receivedSykmeldingMedBehandlingsutfall: ReceivedSykmeldingMedBehandlingsutfall) {
        try {
            kafkaProducer.send(ProducerRecord(topic, sykmeldingId, receivedSykmeldingMedBehandlingsutfall))
        } catch (ex: Exception) {
            log.error("Failed to send gammel sykmelding to kafkatopic {}", sykmeldingId)
            throw ex
        }
    }
}
