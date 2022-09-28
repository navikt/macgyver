package no.nav.syfo.sykmelding

import no.nav.syfo.db.Database
import no.nav.syfo.kafka.SykmeldingEndringsloggKafkaProducer
import no.nav.syfo.log
import no.nav.syfo.model.sykmeldingstatus.STATUS_SLETTET
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO
import no.nav.syfo.persistering.db.postgres.hentSykmeldingMedId
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.OffsetDateTime
import java.time.ZoneOffset

class DeleteSykmeldingService(
    val syfoSmRegisterDb: Database,
    val kafkaProducer: SykmeldingStatusKafkaProducer,
    val endringsloggKafkaProducer: SykmeldingEndringsloggKafkaProducer,
    val tombstoneProducer: KafkaProducer<String, Any?>,
    val topics: List<String>

) {
    fun deleteSykmelding(sykmeldingID: String) {

        val sykmelding = syfoSmRegisterDb.connection.hentSykmeldingMedId(sykmeldingID)
        if (sykmelding != null) {
            endringsloggKafkaProducer.publishToKafka(sykmelding.sykmeldingsdokument!!)
            kafkaProducer.send(
                SykmeldingStatusKafkaEventDTO(
                    sykmeldingID,
                    OffsetDateTime.now(ZoneOffset.UTC),
                    STATUS_SLETTET,
                    null,
                    null
                ),
                "macgyver",
                sykmelding.sykmeldingsopplysninger.pasientFnr
            )
            try {
                topics.forEach { topic ->
                    tombstoneProducer.send(ProducerRecord(topic, sykmeldingID, null)).get()
                }
            } catch (e: Exception) {
                log.error("Kunne ikke skrive tombstone til topic for sykmeldingid $sykmeldingID: {}", e.message)
                throw e
            }
        } else {
            log.warn("Could not find sykmelding with id $sykmeldingID")
            throw DeleteSykmeldingException("Could not find sykmelding with id $sykmeldingID")
        }
    }
}

class DeleteSykmeldingException(override val message: String) : Exception(message)
