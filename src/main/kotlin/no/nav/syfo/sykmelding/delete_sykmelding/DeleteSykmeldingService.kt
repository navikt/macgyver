package no.nav.syfo.sykmelding.delete_sykmelding

import java.time.OffsetDateTime
import java.time.ZoneOffset
import no.nav.syfo.logging.AuditLogger
import no.nav.syfo.logging.auditlogg
import no.nav.syfo.logging.logger
import no.nav.syfo.model.sykmeldingstatus.STATUS_SLETTET
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO
import no.nav.syfo.plugins.UserPrincipal
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

class DeleteSykmeldingService(
    val deleteSykmeldingDatabase: DeleteSykmeldingDatabase,
    val kafkaProducer: SykmeldingStatusKafkaProducer,
    val tombstoneProducer: KafkaProducer<String, Any?>,
    val topics: List<String>,
    val dokArkivClient: DokArkivClient
) {
    suspend fun deleteSykmelding(sykmeldingID: String, journalpostId: String, user: UserPrincipal) {
        val sykmelding = deleteSykmeldingDatabase.hentSykmeldingMedId(sykmeldingID)
        if (sykmelding != null) {
            auditlogg.info(
                AuditLogger(user.email)
                    .createcCefMessage(
                        fnr = sykmelding.sykmeldingsopplysninger.pasientFnr,
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
                    e.message,
                )
                throw e
            }
        } else {
            logger.warn("Could not find sykmelding with id $sykmeldingID")
            throw DeleteSykmeldingException("Could not find sykmelding with id $sykmeldingID")
        }

        dokArkivClient.feilregistreresJournalpost(
            journalpostId = journalpostId,
            sykmeldingId = sykmeldingID,
        )
    }
}

class DeleteSykmeldingException(override val message: String) : Exception(message)
