package no.nav.syfo.sykmelding

import java.time.OffsetDateTime
import java.time.ZoneOffset
import no.nav.syfo.auditlogg
import no.nav.syfo.auditlogger.AuditLogger
import no.nav.syfo.db.Database
import no.nav.syfo.dokarkiv.client.DokArkivClient
import no.nav.syfo.logger
import no.nav.syfo.model.sykmeldingstatus.STATUS_SLETTET
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO
import no.nav.syfo.persistering.db.postgres.hentSykmeldingMedId
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

class DeleteSykmeldingService(
    val syfoSmRegisterDb: Database,
    val kafkaProducer: SykmeldingStatusKafkaProducer,
    val tombstoneProducer: KafkaProducer<String, Any?>,
    val topics: List<String>,
    val dokArkivClient: DokArkivClient
) {
    suspend fun deleteSykmelding(sykmeldingID: String, journalpostId: String, accessToken: String) {
        val sykmelding = syfoSmRegisterDb.connection.hentSykmeldingMedId(sykmeldingID)
        if (sykmelding != null) {
            auditlogg.info(
                AuditLogger()
                    .createcCefMessage(
                        fnr = sykmelding.sykmeldingsopplysninger.pasientFnr,
                        accessToken = accessToken,
                        operation = AuditLogger.Operation.WRITE,
                        requestPath = "/api/sykmelding/$sykmeldingID",
                        permit = AuditLogger.Permit.PERMIT,
                    ),
            )
            kafkaProducer.send(
                SykmeldingStatusKafkaEventDTO(
                    sykmeldingID,
                    OffsetDateTime.now(ZoneOffset.UTC),
                    STATUS_SLETTET,
                    null,
                    null,
                ),
                "macgyver",
                sykmelding.sykmeldingsopplysninger.pasientFnr,
            )
            try {
                topics.forEach { topic ->
                    tombstoneProducer.send(ProducerRecord(topic, sykmeldingID, null)).get()
                }
            } catch (e: Exception) {
                logger.error(
                    "Kunne ikke skrive tombstone til topic for sykmeldingid $sykmeldingID: {}",
                    e.message
                )
                throw e
            }
        } else {
            logger.warn("Could not find sykmelding with id $sykmeldingID")
            throw DeleteSykmeldingException("Could not find sykmelding with id $sykmeldingID")
        }

        dokArkivClient.feilregistreresJournalpost(
            journalpostId = journalpostId,
            sykmeldingId = sykmeldingID
        )
    }
}

class DeleteSykmeldingException(override val message: String) : Exception(message)
