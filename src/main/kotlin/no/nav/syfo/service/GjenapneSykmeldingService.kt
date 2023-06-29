package no.nav.syfo.service

import java.time.OffsetDateTime
import java.time.ZoneOffset
import no.nav.syfo.db.Database
import no.nav.syfo.logger
import no.nav.syfo.model.sykmeldingstatus.STATUS_APEN
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO
import no.nav.syfo.persistering.db.postgres.hentSykmeldingMedId
import no.nav.syfo.sykmelding.SykmeldingStatusKafkaProducer

class GjenapneSykmeldingService(
    private val sykmeldingStatusKafkaProducer: SykmeldingStatusKafkaProducer,
    private val syfoSmRegisterDb: Database,
) {
    fun gjenapneSykmelding(sykmeldingId: String) {
        val sykmelding = syfoSmRegisterDb.connection.hentSykmeldingMedId(sykmeldingId)
        if (sykmelding != null) {
            logger.info("Gjenåpner sykmelding med sykmeldingid {}", sykmeldingId)
            val sykmeldingStatusKafkaEventDTO =
                SykmeldingStatusKafkaEventDTO(
                    sykmeldingId = sykmeldingId,
                    timestamp = OffsetDateTime.now(ZoneOffset.UTC),
                    statusEvent = STATUS_APEN,
                    arbeidsgiver = null,
                    sporsmals = null,
                )
            sykmeldingStatusKafkaProducer.send(
                sykmeldingStatusKafkaEventDTO,
                "migrering",
                sykmelding.sykmeldingsopplysninger.pasientFnr
            )
            logger.info("Sendt statusendring")
        } else {
            logger.info("fant ikke sykmelding med id {}", sykmeldingId)
        }
    }
}
