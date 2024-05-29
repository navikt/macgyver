package no.nav.syfo.narmesteleder

import no.nav.syfo.kafka.aiven.KafkaUtils
import no.nav.syfo.kafka.toProducerConfig
import no.nav.syfo.logging.logger
import no.nav.syfo.narmesteleder.kafkamodel.NlResponseKafkaMessage
import no.nav.syfo.utils.JacksonNullableKafkaSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

interface NarmesteLederResponseKafkaProducer {
    fun publishToKafka(nlResponseKafkaMessage: NlResponseKafkaMessage, orgnummer: String)
}

class NarmesteLederResponseKafkaProducerProduction(
    private val topic: String,
) : NarmesteLederResponseKafkaProducer {

    private val kafkaProducerNlResponse: KafkaProducer<String, NlResponseKafkaMessage> =
        KafkaProducer<String, NlResponseKafkaMessage>(
            KafkaUtils.getAivenKafkaConfig("narmesteleder-request-producer")
                .toProducerConfig(
                    "macgyver-producer",
                    JacksonNullableKafkaSerializer::class,
                    StringSerializer::class,
                ),
        )

    override fun publishToKafka(nlResponseKafkaMessage: NlResponseKafkaMessage, orgnummer: String) {
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

class NarmesteLederResponseKafkaProducerDevelopment() : NarmesteLederResponseKafkaProducer {
    override fun publishToKafka(nlResponseKafkaMessage: NlResponseKafkaMessage, orgnummer: String) {
        logger.info(
            "Publishing narmesteleder for orgnummer $orgnummer to kafka topic $nlResponseKafkaMessage"
        )
    }
}
