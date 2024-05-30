package no.nav.syfo.sykmelding.delete_sykmelding

import java.time.OffsetDateTime
import java.time.ZoneOffset
import no.nav.syfo.logging.AuditLogger
import no.nav.syfo.logging.auditlogg
import no.nav.syfo.logging.logger
import no.nav.syfo.model.sykmeldingstatus.STATUS_SLETTET
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO
import no.nav.syfo.plugins.UserPrincipal

class DeleteSykmeldingService(
    val deleteSykmeldingDatabase: DeleteSykmeldingDatabase,
    val sykmeldingStatusKafkaProducer: SykmeldingStatusKafkaProducer,
    val tombstoneProducer: TombstoneKafkaProducer,
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
            sykmeldingStatusKafkaProducer.send(
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

            tombstoneProducer.send(topics, sykmeldingID)
        } else {
            logger.warn("Could not find sykmelding with id $sykmeldingID")
            throw DeleteSykmeldingException("Could not find sykmelding with id $sykmeldingID")
        }

        dokArkivClient.registrerFeilMedJournalpost(
            journalpostId = journalpostId,
            sykmeldingId = sykmeldingID,
        )
    }
}

class DeleteSykmeldingException(override val message: String) : Exception(message)
