package no.nav.syfo.sykmelding.delete_sykmelding

import java.time.OffsetDateTime
import java.time.ZoneOffset
import no.nav.syfo.kafka.aiven.KafkaUtils
import no.nav.syfo.kafka.toProducerConfig
import no.nav.syfo.logging.logger
import no.nav.syfo.model.sykmeldingstatus.KafkaMetadataDTO
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaMessageDTO
import no.nav.syfo.utils.JacksonKafkaSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

interface SykmeldingStatusKafkaProducer {
    fun send(
        sykmeldingStatusKafkaEventDTO: SykmeldingStatusKafkaEventDTO,
        source: String,
        fnr: String
    )
}

class SykmeldingStatusKafkaProducerProduction(
    private val applicationName: String,
    private val topic: String,
) : SykmeldingStatusKafkaProducer {
    private val kafkaProducer =
        KafkaProducer<String, SykmeldingStatusKafkaMessageDTO>(
            KafkaUtils.getAivenKafkaConfig("sykmelding-status-producer")
                .toProducerConfig(
                    groupId = this.applicationName,
                    valueSerializer = JacksonKafkaSerializer::class,
                    keySerializer = StringSerializer::class,
                ),
        )

    override fun send(
        sykmeldingStatusKafkaEventDTO: SykmeldingStatusKafkaEventDTO,
        source: String,
        fnr: String
    ) {
        logger.info(
            "Skriver statusendring for sykmelding med id {} til topic",
            sykmeldingStatusKafkaEventDTO.sykmeldingId,
        )

        val metadataDTO =
            KafkaMetadataDTO(
                sykmeldingId = sykmeldingStatusKafkaEventDTO.sykmeldingId,
                timestamp =
                    OffsetDateTime.now(
                        ZoneOffset.UTC,
                    ),
                fnr = fnr,
                source = source,
            )

        val sykmeldingStatusKafkaMessageDTO =
            SykmeldingStatusKafkaMessageDTO(metadataDTO, sykmeldingStatusKafkaEventDTO)

        try {
            kafkaProducer
                .send(
                    ProducerRecord(
                        topic,
                        sykmeldingStatusKafkaMessageDTO.event.sykmeldingId,
                        sykmeldingStatusKafkaMessageDTO,
                    ),
                )
                .get()
        } catch (ex: Exception) {
            logger.error(
                "Failed to send sykmeldingStatus to kafkatopic {}",
                metadataDTO.sykmeldingId,
            )
            throw ex
        }
    }
}

class SykmeldingStatusKafkaProducerDevelopment : SykmeldingStatusKafkaProducer {
    override fun send(
        sykmeldingStatusKafkaEventDTO: SykmeldingStatusKafkaEventDTO,
        source: String,
        fnr: String
    ) {
        logger.info(
            "Skriver statusendring for sykmelding med id {} til topic",
            sykmeldingStatusKafkaEventDTO.sykmeldingId,
        )
    }
}
