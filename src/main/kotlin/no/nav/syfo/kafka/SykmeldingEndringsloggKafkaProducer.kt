package no.nav.syfo.kafka

import no.nav.syfo.log
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

class SykmeldingEndringsloggKafkaProducer(
    private val endringsloggTopic: String,
    private val kafkaproducerEndringsloggSykmelding: KafkaProducer<String, String>
) {

    fun publishToKafka(sykmelding: String) {
        try {
            kafkaproducerEndringsloggSykmelding.send(ProducerRecord(endringsloggTopic, sykmelding)).get()
        } catch (e: Exception) {
            log.error("Noe gikk galt ved skriving til endringslogg-topic: ", e.cause)
            throw e
        }
    }
}
