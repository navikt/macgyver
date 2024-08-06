package no.nav.syfo.narmesteleder

import no.nav.syfo.kafka.aiven.KafkaUtils
import no.nav.syfo.kafka.toProducerConfig
import no.nav.syfo.logging.logger
import no.nav.syfo.narmesteleder.kafkamodel.NlRequestKafkaMessage
import no.nav.syfo.utils.JacksonNullableKafkaSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

interface NarmesteLederRequestKafkaProducer {
    fun publishToKafka(nlRequestKafkaMessage: NlRequestKafkaMessage, orgnummer: String)
}

class NarmesteLederRequestKafkaProducerProduction(
    private val topic: String,
) : NarmesteLederRequestKafkaProducer {

    private val kafkaProducerNlRequest: KafkaProducer<String, NlRequestKafkaMessage> =
        KafkaProducer<String, NlRequestKafkaMessage>(
            KafkaUtils.getAivenKafkaConfig("narmesteleder-request-producer")
                .toProducerConfig(
                    "macgyver-producer",
                    JacksonNullableKafkaSerializer::class,
                    StringSerializer::class,
                ),
        )

    override fun publishToKafka(nlRequestKafkaMessage: NlRequestKafkaMessage, orgnummer: String) {
        try {
            kafkaProducerNlRequest
                .send(
                    ProducerRecord(
                        topic,
                        orgnummer,
                        nlRequestKafkaMessage,
                    ),
                )
                .get()
        } catch (e: Exception) {
            logger.error("Noe gikk galt ved skriving av nlResponse: ${e.message}")
            throw e
        }
    }
}

class NarmesteLederRequestKafkaProducerDevelopment : NarmesteLederRequestKafkaProducer {
    override fun publishToKafka(nlRequestKafkaMessage: NlRequestKafkaMessage, orgnummer: String) {
        logger.info("Sending narmestelederrequest to orgnummer $orgnummer")
    }
}
