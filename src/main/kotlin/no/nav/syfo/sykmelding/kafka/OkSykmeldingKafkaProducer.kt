package no.nav.syfo.sykmelding.kafka

import no.nav.syfo.log
import no.nav.syfo.model.ReceivedSykmelding
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

class OkSykmeldingKafkaProducer(
    private val kafkaproducerReceivedSykmelding: KafkaProducer<String, ReceivedSykmelding>,
    private val okTopic: String
) {
    fun sendReceivedSykmelding(receivedSykmelding: ReceivedSykmelding) {
        try {
            kafkaproducerReceivedSykmelding.send(
                ProducerRecord(okTopic, receivedSykmelding.sykmelding.id, receivedSykmelding)
            ).get()
            log.info("Sykmelding sendt til kafkatopic $okTopic sykmelding id ${receivedSykmelding.sykmelding.id}")
        } catch (ex: Exception) {
            log.error("Noe gikk galt ved sending av sykmelding med id ${receivedSykmelding.sykmelding.id} til topic")
            throw ex
        }
    }
}
