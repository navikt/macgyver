package no.nav.syfo.sykmelding.gamlesykmeldinger.kafka

import no.nav.syfo.log
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

class SykmeldingIdKafkaProducer(
    private val kafkaProducer: KafkaProducer<String, String>,
    private val topic: String
) {
    fun send(sykmeldingId: String) {
        try {
            kafkaProducer.send(ProducerRecord(topic, sykmeldingId, sykmeldingId))
        } catch (ex: Exception) {
            log.error("Failed to send sykmeldingid to kafkatopic {}", sykmeldingId)
            throw ex
        }
    }
}
