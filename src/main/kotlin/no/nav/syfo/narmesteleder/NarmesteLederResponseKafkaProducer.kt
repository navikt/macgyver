package no.nav.syfo.narmesteleder

import no.nav.syfo.narmesteleder.kafkamodel.NlResponseKafkaMessage
import no.nav.syfo.utils.logger
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

class NarmesteLederResponseKafkaProducer(
    private val topic: String,
    private val kafkaProducerNlResponse: KafkaProducer<String, NlResponseKafkaMessage>,
) {

    fun publishToKafka(nlResponseKafkaMessage: NlResponseKafkaMessage, orgnummer: String) {
        try {
            kafkaProducerNlResponse
                .send(
                    ProducerRecord(
                        topic,
                        orgnummer,
                        nlResponseKafkaMessage,
                    ),
                )
                .get()
        } catch (e: Exception) {
            logger.error("Noe gikk galt ved skriving av nlResponse: ${e.message}")
            throw e
        }
    }
}
