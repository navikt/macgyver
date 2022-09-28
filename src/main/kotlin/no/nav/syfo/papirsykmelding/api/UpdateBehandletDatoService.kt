package no.nav.syfo.papirsykmelding.api

import no.nav.syfo.db.Database
import no.nav.syfo.kafka.SykmeldingEndringsloggKafkaProducer
import no.nav.syfo.log
import no.nav.syfo.objectMapper
import no.nav.syfo.persistering.db.postgres.hentSykmeldingsdokument
import no.nav.syfo.persistering.db.postgres.updateBehandletTidspunkt
import java.time.LocalDate

class UpdateBehandletDatoService(
    private val syfoSmRegisterDb: Database,
    private val sykmeldingEndringsloggKafkaProducer: SykmeldingEndringsloggKafkaProducer
) {
    fun updateBehandletDato(sykmeldingId: String, behandletDato: LocalDate) {
        val sykmeldingsdokument = syfoSmRegisterDb.connection.hentSykmeldingsdokument(sykmeldingId)
        val oppdatertBehandletTidspunkt = behandletDato.atTime(12, 0)

        if (sykmeldingsdokument != null) {
            log.info(
                "Endrer behandletDato fra ${objectMapper.writeValueAsString(sykmeldingsdokument.sykmelding.behandletTidspunkt)}" +
                        " til ${objectMapper.writeValueAsString(oppdatertBehandletTidspunkt)} for id $sykmeldingId"
            )
            sykmeldingEndringsloggKafkaProducer.publishToKafka(sykmeldingsdokument)

            syfoSmRegisterDb.updateBehandletTidspunkt(sykmeldingId, oppdatertBehandletTidspunkt)

            log.info("BehandletDato er oppdatert for sykmeldingId: $sykmeldingId")
        } else {
            log.info("Fant ikke sykmelding med id {}", sykmeldingId)
            throw RuntimeException("Fant ikke sykmelding med id $sykmeldingId")
        }
    }
}
