package no.nav.syfo.sykmelding.delete_sykmelding

import no.nav.syfo.kafka.aiven.KafkaUtils
import no.nav.syfo.kafka.toProducerConfig
import no.nav.syfo.logging.logger
import no.nav.syfo.utils.JacksonNullableKafkaSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

interface TombstoneKafkaProducer {
    fun send(
        topics: List<String>,
        id: String,
    )
}

class TombstoneKafkaProducerProduction : TombstoneKafkaProducer {
    private val tombstoneProducer =
        KafkaProducer<String, Any?>(
            KafkaUtils.getAivenKafkaConfig("delete-sykmelding-status-producer")
                .toProducerConfig(
                    "macgyver-tombstone-producer",
                    JacksonNullableKafkaSerializer::class,
                ),
        )

    override fun send(
        topics: List<String>,
        id: String,
    ) {
        try {
            topics.forEach { topic ->
                tombstoneProducer.send(ProducerRecord(topic, id, null)).get()
                logger.info("Skriver tombstone til topic: $topic for id $id til topic")
            }
        } catch (e: Exception) {
            logger.error(
                "Kunne ikke skrive tombstone til topic for id $id: {}",
                e.message,
            )
            throw e
        }
    }
}

class TombstoneKafkaProducerDevelopment : TombstoneKafkaProducer {
    override fun send(
        topics: List<String>,
        id: String,
    ) {
        logger.info("Skriver tombstone for id: $id til topic")
    }
}
