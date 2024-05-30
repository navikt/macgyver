package no.nav.syfo.sykmelding.aivenmigrering

import no.nav.syfo.logging.logger
import no.nav.syfo.utils.objectMapper
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

interface SykmeldingV2KafkaProducer {
    fun sendSykmelding(
        sykmeldingKafkaMessage: SykmeldingV2KafkaMessage?,
        sykmeldingId: String,
        topic: String
    )
}

class SykmeldingV2KafkaProducerProduction(
    private val kafkaProducer: KafkaProducer<String, SykmeldingV2KafkaMessage?>,
) : SykmeldingV2KafkaProducer {
    override fun sendSykmelding(
        sykmeldingKafkaMessage: SykmeldingV2KafkaMessage?,
        sykmeldingId: String,
        topic: String
    ) {
        try {
            kafkaProducer.send(ProducerRecord(topic, sykmeldingId, sykmeldingKafkaMessage)).get()
        } catch (e: Exception) {
            logger.error("Noe gikk galt ved skriving til topic $topic, sykmeldingsid $sykmeldingId")
            throw e
        }
    }
}

class SykmeldingV2KafkaProducerDevelopment() : SykmeldingV2KafkaProducer {
    override fun sendSykmelding(
        sykmeldingKafkaMessage: SykmeldingV2KafkaMessage?,
        sykmeldingId: String,
        topic: String
    ) {
        logger.info("Sending sykmelding $sykmeldingId")
        logger.info(objectMapper.writeValueAsString(sykmeldingKafkaMessage))
    }
}
