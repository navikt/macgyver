package no.nav.syfo.papirsykmelding.api

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import no.nav.syfo.db.Database
import no.nav.syfo.getReceivedSykmelding
import no.nav.syfo.kafka.SykmeldingEndringsloggKafkaProducer
import no.nav.syfo.model.Behandlingsutfall
import no.nav.syfo.model.Status
import no.nav.syfo.model.ValidationResult
import no.nav.syfo.sykmelding.db.ReceivedSykmeldingMedBehandlingsutfall
import no.nav.syfo.sykmelding.db.getSykmelding
import no.nav.syfo.sykmelding.kafka.OkSykmeldingKafkaProducer
import no.nav.syfo.sykmelding.model.AktivitetIkkeMulig
import no.nav.syfo.sykmelding.model.MedisinskArsak
import no.nav.syfo.sykmelding.model.MedisinskArsakType
import no.nav.syfo.sykmelding.model.Periode
import org.amshove.kluent.internal.assertFailsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class UpdatePeriodeServiceTest {
    private val database = mockk<Database>()
    private val sykmeldingEndringsloggKafkaProducer = mockk<SykmeldingEndringsloggKafkaProducer>(relaxed = true)
    private val okSykmeldingKafkaProducer = mockk<OkSykmeldingKafkaProducer>(relaxed = true)

    private val updatePeriodeService = UpdatePeriodeService(database, sykmeldingEndringsloggKafkaProducer, okSykmeldingKafkaProducer)

    @BeforeEach
    fun before() {
        mockkStatic("no.nav.syfo.sykmelding.db.SyfosmregisterQueriesKt")
        clearMocks(okSykmeldingKafkaProducer, database)
    }

    @Test
    fun senderOppdatertSykmeldingTilTopic() {
        val sykmeldingId = UUID.randomUUID().toString()
        every { database.getSykmelding(sykmeldingId) } returns ReceivedSykmeldingMedBehandlingsutfall(
            receivedSykmelding = getReceivedSykmelding(sykmeldingId),
            behandlingsutfall = Behandlingsutfall(
                id = sykmeldingId,
                behandlingsutfall = ValidationResult(Status.OK, emptyList())
            )
        )

        updatePeriodeService.updatePeriode(
            sykmeldingId,
            listOf(
                Periode(
                    fom = LocalDate.now().minusWeeks(3),
                    tom = LocalDate.now().minusWeeks(1),
                    aktivitetIkkeMulig = AktivitetIkkeMulig(MedisinskArsak("beskrivelse", listOf(MedisinskArsakType.AKTIVITET_FORVERRER_TILSTAND)), null),
                    avventendeInnspillTilArbeidsgiver = null,
                    behandlingsdager = null,
                    gradert = null,
                    reisetilskudd = false
                )
            )
        )
        verify {
            okSykmeldingKafkaProducer.sendReceivedSykmelding(
                match {
                    it.sykmelding.perioder[0].fom == LocalDate.now().minusWeeks(3) &&
                        it.sykmelding.perioder[0].aktivitetIkkeMulig?.medisinskArsak?.arsak?.get(0) == no.nav.syfo.model.MedisinskArsakType.AKTIVITET_FORVERRER_TILSTAND
                }
            )
        }
    }

    @Test
    fun feilerHvisSykmeldingErAvvist() {
        val sykmeldingId = UUID.randomUUID().toString()
        every { database.getSykmelding(sykmeldingId) } returns ReceivedSykmeldingMedBehandlingsutfall(
            receivedSykmelding = getReceivedSykmelding(sykmeldingId),
            behandlingsutfall = Behandlingsutfall(
                id = sykmeldingId,
                behandlingsutfall = ValidationResult(Status.INVALID, emptyList())
            )
        )

        assertFailsWith<IllegalArgumentException> {
            updatePeriodeService.updatePeriode(
                sykmeldingId,
                listOf(
                    Periode(
                        fom = LocalDate.now().minusWeeks(3),
                        tom = LocalDate.now().minusWeeks(1),
                        aktivitetIkkeMulig = AktivitetIkkeMulig(MedisinskArsak(null, emptyList()), null),
                        avventendeInnspillTilArbeidsgiver = null,
                        behandlingsdager = null,
                        gradert = null,
                        reisetilskudd = false
                    )
                )
            )
        }
        verify(exactly = 0) { okSykmeldingKafkaProducer.sendReceivedSykmelding(any()) }
    }
}
