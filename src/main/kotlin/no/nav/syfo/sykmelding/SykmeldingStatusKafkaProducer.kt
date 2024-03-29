package no.nav.syfo.sykmelding

import java.time.OffsetDateTime
import java.time.ZoneOffset
import no.nav.syfo.logger
import no.nav.syfo.model.sykmeldingstatus.KafkaMetadataDTO
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaMessageDTO
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

class SykmeldingStatusKafkaProducer(
    private val kafkaProducer: KafkaProducer<String, SykmeldingStatusKafkaMessageDTO>,
    private val topic: String,
) {
    fun send(
        sykmeldingStatusKafkaEventDTO: SykmeldingStatusKafkaEventDTO,
        source: String,
        fnr: String
    ) {
        logger.info(
            "Skriver statusendring for sykmelding med id {} til topic",
            sykmeldingStatusKafkaEventDTO.sykmeldingId
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
                        sykmeldingStatusKafkaMessageDTO
                    )
                )
                .get()
        } catch (ex: Exception) {
            logger.error(
                "Failed to send sykmeldingStatus to kafkatopic {}",
                metadataDTO.sykmeldingId
            )
            throw ex
        }
    }
}
