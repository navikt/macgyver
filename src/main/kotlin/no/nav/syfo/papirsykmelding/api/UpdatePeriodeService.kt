package no.nav.syfo.papirsykmelding.api

import no.nav.syfo.db.Database
import no.nav.syfo.kafka.SykmeldingEndringsloggKafkaProducer
import no.nav.syfo.log
import no.nav.syfo.model.ArbeidsrelatertArsak
import no.nav.syfo.model.Gradert
import no.nav.syfo.model.MedisinskArsak
import no.nav.syfo.model.Status
import no.nav.syfo.objectMapper
import no.nav.syfo.sykmelding.db.getSykmelding
import no.nav.syfo.sykmelding.kafka.OkSykmeldingKafkaProducer
import no.nav.syfo.sykmelding.model.AktivitetIkkeMulig
import no.nav.syfo.sykmelding.model.ArbeidsrelatertArsakType
import no.nav.syfo.sykmelding.model.MedisinskArsakType
import no.nav.syfo.sykmelding.model.Periode

class UpdatePeriodeService(
    private val databasePostgres: Database,
    private val sykmeldingEndringsloggKafkaProducer: SykmeldingEndringsloggKafkaProducer,
    private val okSykmeldingKafkaProducer: OkSykmeldingKafkaProducer
) {
    fun updatePeriode(sykmeldingId: String, periodeliste: List<Periode>) {
        val sykmeldingMedBehandlingsutfall = databasePostgres.getSykmelding(sykmeldingId)
        log.info("Hentet sykmelding")

        if (sykmeldingMedBehandlingsutfall != null) {
            if (sykmeldingMedBehandlingsutfall.behandlingsutfall.behandlingsutfall.status == Status.INVALID) {
                log.error("Kan ikke endre en avvist sykmelding!")
                throw IllegalArgumentException("Kan ikke endre en avvist sykmelding")
            }
            val receivedSykmelding = sykmeldingMedBehandlingsutfall.receivedSykmelding
            log.info(
                "Endrer perioder fra ${objectMapper.writeValueAsString(receivedSykmelding.sykmelding.perioder)}" +
                    " til ${objectMapper.writeValueAsString(periodeliste)} for id $sykmeldingId"
            )
            sykmeldingEndringsloggKafkaProducer.publishToKafka(objectMapper.writeValueAsString(receivedSykmelding))
            val oppdatertSykmeldingsdokument = receivedSykmelding.sykmelding.copy(
                perioder = periodeliste.map { it.tilReceivedSykmeldingPeriode() }
            )
            val oppdatertSykmelding = receivedSykmelding.copy(sykmelding = oppdatertSykmeldingsdokument)

            okSykmeldingKafkaProducer.sendReceivedSykmelding(oppdatertSykmelding)
        } else {
            log.info("Fant ikke sykmelding med id {}", sykmeldingId)
            throw RuntimeException("Fant ikke sykmelding med id $sykmeldingId")
        }
    }
}

fun Periode.tilReceivedSykmeldingPeriode(): no.nav.syfo.model.Periode {
    return no.nav.syfo.model.Periode(
        fom = fom,
        tom = tom,
        aktivitetIkkeMulig = aktivitetIkkeMulig?.tilReceivedSykmeldingAktivitetIkkeMulig(),
        avventendeInnspillTilArbeidsgiver = avventendeInnspillTilArbeidsgiver,
        behandlingsdager = behandlingsdager,
        gradert = gradert?.let { Gradert(reisetilskudd = it.reisetilskudd, grad = it.grad) },
        reisetilskudd = reisetilskudd
    )
}

fun AktivitetIkkeMulig.tilReceivedSykmeldingAktivitetIkkeMulig(): no.nav.syfo.model.AktivitetIkkeMulig {
    return no.nav.syfo.model.AktivitetIkkeMulig(
        medisinskArsak = medisinskArsak?.let {
            MedisinskArsak(
                beskrivelse = it.beskrivelse,
                arsak = it.arsak.map { arsakType -> arsakType.tilReceivedSykmeldingMedisinskArsakType() }
            )
        },
        arbeidsrelatertArsak = arbeidsrelatertArsak?.let {
            ArbeidsrelatertArsak(
                beskrivelse = it.beskrivelse,
                arsak = it.arsak.map { arsakType -> arsakType.tilReceivedSykmeldingArbeidsrelatertArsakType() }
            )
        }
    )
}

fun MedisinskArsakType.tilReceivedSykmeldingMedisinskArsakType(): no.nav.syfo.model.MedisinskArsakType {
    return enumValueOf(name)
}

fun ArbeidsrelatertArsakType.tilReceivedSykmeldingArbeidsrelatertArsakType(): no.nav.syfo.model.ArbeidsrelatertArsakType {
    return enumValueOf(name)
}
