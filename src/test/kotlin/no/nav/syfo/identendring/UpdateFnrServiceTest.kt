package no.nav.syfo.identendring

import io.ktor.server.testing.*
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.assertFailsWith
import kotlinx.coroutines.runBlocking
import no.nav.helse.diagnosekoder.Diagnosekoder
import no.nav.syfo.identendring.update_fnr.Adresse
import no.nav.syfo.identendring.update_fnr.AktivitetIkkeMulig
import no.nav.syfo.identendring.update_fnr.Arbeidsgiver
import no.nav.syfo.identendring.update_fnr.ArbeidsgiverDbModel
import no.nav.syfo.identendring.update_fnr.AvsenderSystem
import no.nav.syfo.identendring.update_fnr.Behandler
import no.nav.syfo.identendring.update_fnr.Diagnose
import no.nav.syfo.identendring.update_fnr.HarArbeidsgiver
import no.nav.syfo.identendring.update_fnr.KontaktMedPasient
import no.nav.syfo.identendring.update_fnr.MedisinskArsak
import no.nav.syfo.identendring.update_fnr.MedisinskVurdering
import no.nav.syfo.identendring.update_fnr.MeldingTilNAV
import no.nav.syfo.identendring.update_fnr.Periode
import no.nav.syfo.identendring.update_fnr.StatusDbModel
import no.nav.syfo.identendring.update_fnr.Sykmelding
import no.nav.syfo.identendring.update_fnr.SykmeldingDbModelUtenBehandlingsutfall
import no.nav.syfo.identendring.update_fnr.UpdateFnrDatabase
import no.nav.syfo.identendring.update_fnr.UpdateFnrService
import no.nav.syfo.identendring.update_fnr.UpdateIdentException
import no.nav.syfo.narmesteleder.NarmesteLeder
import no.nav.syfo.narmesteleder.NarmesteLederResponseKafkaProducer
import no.nav.syfo.narmesteleder.NarmestelederClient
import no.nav.syfo.narmesteleder.kafkamodel.Leder
import no.nav.syfo.narmesteleder.kafkamodel.NlResponse
import no.nav.syfo.narmesteleder.kafkamodel.NlResponseKafkaMessage
import no.nav.syfo.narmesteleder.kafkamodel.Sykmeldt
import no.nav.syfo.pdl.PdlPersonService
import no.nav.syfo.pdl.client.model.IdentInformasjon
import no.nav.syfo.pdl.model.PdlPerson
import no.nav.syfo.sykmelding.aivenmigrering.SykmeldingV2KafkaProducer
import no.nav.syfo.utils.setupTestApplication
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import org.koin.dsl.module

internal class UpdateFnrServiceTest {
    private val pdlPersonService = mockk<PdlPersonService>(relaxed = true)
    private val db = mockk<UpdateFnrDatabase>(relaxed = true)
    private val sendtSykmeldingKafkaProducer = mockk<SykmeldingV2KafkaProducer>(relaxed = true)
    private val narmesteLederResponseKafkaProducer =
        mockk<NarmesteLederResponseKafkaProducer>(relaxed = true)
    private val narmestelederClient = mockk<NarmestelederClient>()

    private val updateFnrService =
        UpdateFnrService(
            pdlPersonService,
            db,
            sendtSykmeldingKafkaProducer,
            narmesteLederResponseKafkaProducer,
            narmestelederClient,
            "topic",
        )

    @BeforeEach
    fun before() = testApplication {
        setupTestApplication { dependencies { modules(module { single { updateFnrService } }) } }
        clearMocks(sendtSykmeldingKafkaProducer, narmesteLederResponseKafkaProducer)
    }

    @AfterEach fun cleanup() = stopKoin()

    @Test
    internal fun `Skal oppdatere OK hvis nytt og gammelt fnr er knyttet til samme person`() =
        testApplication {
            coEvery { pdlPersonService.getPdlPerson(any()) } returns
                PdlPerson(
                    listOf(
                        IdentInformasjon("12345678913", false, "FOLKEREGISTERIDENT"),
                        IdentInformasjon("12345678912", true, "FOLKEREGISTERIDENT"),
                        IdentInformasjon("12345", false, "AKTORID"),
                    ),
                    "navn navn",
                )
            coEvery { narmestelederClient.getNarmesteledere(any()) } returns emptyList()

            every { db.updateFnr(any(), any()) } returns 1

            runBlocking {
                assertEquals(
                    true,
                    updateFnrService.updateFnr(
                        fnr = "12345678912",
                        nyttFnr = "12345678913",
                    ),
                )
            }
        }

    @Test
    internal fun `Skal kaste feil hvis nytt og gammelt fnr ikke er knyttet til samme person`() =
        testApplication {
            coEvery { pdlPersonService.getPdlPerson(any()) } returns
                PdlPerson(
                    listOf(
                        IdentInformasjon("12345678913", false, "FOLKEREGISTERIDENT"),
                        IdentInformasjon("12345678912", true, "FOLKEREGISTERIDENT"),
                        IdentInformasjon("12345", false, "AKTORID"),
                    ),
                    "navn navn",
                )

            every { db.updateFnr(any(), any()) } returns 1

            runBlocking {
                val assertFailsWith =
                    assertFailsWith<UpdateIdentException> {
                        updateFnrService.updateFnr(
                            fnr = "12345678912",
                            nyttFnr = "12345678914",
                        )
                    }
                assertEquals(
                    "Oppdatering av fnr feilet, nyttFnr står ikke som aktivt fnr for aktøren i PDL",
                    assertFailsWith.message,
                )
            }
        }

    @Test
    internal fun `Skal kaste feil hvis fnr ikke er registrert som historisk for person`() =
        testApplication {
            coEvery { pdlPersonService.getPdlPerson(any()) } returns
                PdlPerson(
                    listOf(
                        IdentInformasjon("12345678913", false, "FOLKEREGISTERIDENT"),
                        IdentInformasjon("123", true, "FOLKEREGISTERIDENT"),
                        IdentInformasjon("12345", false, "AKTORID"),
                    ),
                    "navn navn",
                )

            every { db.updateFnr(any(), any()) } returns 1

            runBlocking {
                val assertFailsWith =
                    assertFailsWith<UpdateIdentException> {
                        updateFnrService.updateFnr(
                            fnr = "12345678912",
                            nyttFnr = "12345678913",
                        )
                    }
                assertEquals(
                    "Oppdatering av fnr feilet, fnr er ikke historisk for aktør",
                    assertFailsWith.message,
                )
            }
        }

    @Test
    internal fun `Oppdaterer sendte sykmeldinger og aktiv NL-relasjon`() = testApplication {
        coEvery { pdlPersonService.getPdlPerson(any()) } returns
            PdlPerson(
                listOf(
                    IdentInformasjon("12345678913", false, "FOLKEREGISTERIDENT"),
                    IdentInformasjon("12345678912", true, "FOLKEREGISTERIDENT"),
                    IdentInformasjon("12345", false, "AKTORID"),
                ),
                "navn navn",
            )
        every { db.getSykmeldingerMedFnrUtenBehandlingsutfall("12345678912") } returns
            listOf(getSendtSykmelding())
        coEvery { narmestelederClient.getNarmesteledere(any()) } returns listOf(getNarmesteLeder())

        every { db.updateFnr(any(), any()) } returns 1

        runBlocking {
            assertEquals(
                true,
                updateFnrService.updateFnr(
                    fnr = "12345678912",
                    nyttFnr = "12345678913",
                ),
            )

            coVerify {
                sendtSykmeldingKafkaProducer.sendSykmelding(
                    match { it.kafkaMetadata.fnr == "12345678913" },
                    any(),
                    any(),
                )
            }
            coVerify(exactly = 1) {
                narmesteLederResponseKafkaProducer.publishToKafka(
                    match { it.nlAvbrutt?.sykmeldtFnr == "12345678912" },
                    "9898",
                )
            }
            coVerify(exactly = 1) {
                narmesteLederResponseKafkaProducer.publishToKafka(
                    match { it.nlResponse == getExpectedNarmestelederResponse() },
                    "9898",
                )
            }
        }
    }

    @Test
    internal fun `Oppdaterer kun sendte sykmeldinger fra de siste fire maaneder og kun aktiv NL-relasjon`() =
        testApplication {
            coEvery { pdlPersonService.getPdlPerson(any()) } returns
                PdlPerson(
                    listOf(
                        IdentInformasjon("12345678913", false, "FOLKEREGISTERIDENT"),
                        IdentInformasjon("12345678912", true, "FOLKEREGISTERIDENT"),
                        IdentInformasjon("12345", false, "AKTORID"),
                    ),
                    "navn navn",
                )
            every { db.getSykmeldingerMedFnrUtenBehandlingsutfall("12345678912") } returns
                listOf(
                    getSendtSykmelding(),
                    getSendtSykmelding()
                        .copy(
                            status =
                                StatusDbModel(
                                    "APEN",
                                    OffsetDateTime.now(ZoneOffset.UTC),
                                    null,
                                ),
                        ),
                    getSendtSykmelding(
                        listOf(
                            Periode(
                                fom = LocalDate.now().minusMonths(6),
                                tom = LocalDate.now().minusMonths(5),
                                aktivitetIkkeMulig =
                                    AktivitetIkkeMulig(MedisinskArsak(null, emptyList()), null),
                                avventendeInnspillTilArbeidsgiver = null,
                                behandlingsdager = 0,
                                gradert = null,
                                reisetilskudd = false,
                            ),
                        ),
                    ),
                )
            coEvery { narmestelederClient.getNarmesteledere(any()) } returns
                listOf(
                    getNarmesteLeder(),
                    getNarmesteLeder()
                        .copy(
                            narmesteLederFnr = "987",
                            orgnummer = "9999",
                            aktivTom = LocalDate.now(),
                        ),
                )

            every { db.updateFnr(any(), any()) } returns 1

            runBlocking {
                assertEquals(
                    true,
                    updateFnrService.updateFnr(
                        fnr = "12345678912",
                        nyttFnr = "12345678913",
                    ),
                )

                coVerify(exactly = 1) {
                    sendtSykmeldingKafkaProducer.sendSykmelding(
                        match { it.kafkaMetadata.fnr == "12345678913" },
                        any(),
                        any(),
                    )
                }
                coVerify(exactly = 1) {
                    narmesteLederResponseKafkaProducer.publishToKafka(
                        match<NlResponseKafkaMessage> {
                            it.nlAvbrutt?.sykmeldtFnr == "12345678912"
                        },
                        "9898",
                    )
                }
                coVerify(exactly = 1) {
                    narmesteLederResponseKafkaProducer.publishToKafka(
                        match<NlResponseKafkaMessage> {
                            it.nlResponse == getExpectedNarmestelederResponse()
                        },
                        "9898",
                    )
                }
            }
        }

    @Test
    internal fun `Skal kaste feil hvis nytt og gammelt fnr ikke er knyttet til samme person for leder`() =
        testApplication {
            coEvery { pdlPersonService.getPdlPerson(any()) } returns
                PdlPerson(
                    listOf(
                        IdentInformasjon("12345678913", false, "FOLKEREGISTERIDENT"),
                        IdentInformasjon("12345678912", true, "FOLKEREGISTERIDENT"),
                        IdentInformasjon("12345", false, "AKTORID"),
                    ),
                    "navn navn",
                )

            runBlocking {
                val assertFailsWith =
                    assertFailsWith<UpdateIdentException> {
                        updateFnrService.updateNlFnr(
                            fnr = "12345678912",
                            nyttFnr = "12345678914",
                        )
                    }
                assertEquals(
                    "Oppdatering av leders fnr feilet, nyttFnr står ikke som aktivt fnr for aktøren i PDL",
                    assertFailsWith.message,
                )
            }
        }

    @Test
    internal fun `Skal kaste feil hvis fnr ikke er registrert som historisk for person for leder`() =
        testApplication {
            coEvery { pdlPersonService.getPdlPerson(any()) } returns
                PdlPerson(
                    listOf(
                        IdentInformasjon("12345678913", false, "FOLKEREGISTERIDENT"),
                        IdentInformasjon("123", true, "FOLKEREGISTERIDENT"),
                        IdentInformasjon("12345", false, "AKTORID"),
                    ),
                    "navn navn",
                )

            runBlocking {
                val assertFailsWith =
                    assertFailsWith<UpdateIdentException> {
                        updateFnrService.updateNlFnr(
                            fnr = "12345678912",
                            nyttFnr = "12345678913",
                        )
                    }
                assertEquals(
                    "Oppdatering av leders fnr feilet, fnr er ikke historisk for aktør",
                    assertFailsWith.message,
                )
            }
        }

    @Test
    internal fun `Oppdaterer aktiv NL-relasjon for leder`() = testApplication {
        coEvery { pdlPersonService.getPdlPerson(any()) } returns
            PdlPerson(
                listOf(
                    IdentInformasjon("12345678913", false, "FOLKEREGISTERIDENT"),
                    IdentInformasjon("12345678912", true, "FOLKEREGISTERIDENT"),
                    IdentInformasjon("12345", false, "AKTORID"),
                ),
                "navn navn",
            )
        coEvery { narmestelederClient.getNarmestelederKoblingerForLeder("12345678912") } returns
            listOf(
                getNarmesteLeder()
                    .copy(
                        fnr = "10987654321",
                        narmesteLederFnr = "12345678912",
                    ),
            )

        runBlocking {
            assertEquals(
                true,
                updateFnrService.updateNlFnr(
                    fnr = "12345678912",
                    nyttFnr = "12345678913",
                ),
            )

            coVerify(exactly = 1) {
                narmesteLederResponseKafkaProducer.publishToKafka(
                    match<NlResponseKafkaMessage> { it.nlAvbrutt?.sykmeldtFnr == "10987654321" },
                    "9898",
                )
            }
            coVerify(exactly = 1) {
                narmesteLederResponseKafkaProducer.publishToKafka(
                    match<NlResponseKafkaMessage> {
                        it.nlResponse ==
                            NlResponse(
                                orgnummer = "9898",
                                utbetalesLonn = true,
                                leder =
                                    Leder(
                                        fnr = "12345678913",
                                        mobil = "90909090",
                                        epost = "mail@nav.no",
                                        fornavn = null,
                                        etternavn = null,
                                    ),
                                sykmeldt = Sykmeldt(fnr = "10987654321", navn = null),
                                aktivFom =
                                    LocalDate.of(2019, 2, 2)
                                        .atStartOfDay()
                                        .atOffset(ZoneOffset.UTC),
                                aktivTom = null,
                            )
                    },
                    "9898",
                )
            }
        }
    }
}

fun getSendtSykmelding(
    periodeListe: List<Periode>? = null
): SykmeldingDbModelUtenBehandlingsutfall {
    val id = UUID.randomUUID().toString()
    return SykmeldingDbModelUtenBehandlingsutfall(
        id = id,
        mottattTidspunkt = OffsetDateTime.now(ZoneOffset.UTC).minusMonths(1),
        legekontorOrgNr = "8888",
        sykmeldingsDokument =
            Sykmelding(
                id = id,
                arbeidsgiver =
                    Arbeidsgiver(
                        harArbeidsgiver = HarArbeidsgiver.EN_ARBEIDSGIVER,
                        navn = "navn",
                        stillingsprosent = null,
                        yrkesbetegnelse = null,
                    ),
                medisinskVurdering =
                    MedisinskVurdering(
                        hovedDiagnose = Diagnose(Diagnosekoder.ICPC2_CODE, "L87", null),
                        biDiagnoser = emptyList(),
                        yrkesskade = false,
                        svangerskap = false,
                        annenFraversArsak = null,
                        yrkesskadeDato = null,
                    ),
                andreTiltak = "Andre tiltak",
                meldingTilArbeidsgiver = null,
                navnFastlege = null,
                tiltakArbeidsplassen = null,
                syketilfelleStartDato = null,
                tiltakNAV = "Tiltak NAV",
                prognose = null,
                meldingTilNAV = MeldingTilNAV(true, "Masse bistand"),
                skjermesForPasient = false,
                behandletTidspunkt = LocalDateTime.now(),
                behandler =
                    Behandler(
                        "fornavn",
                        null,
                        "etternavn",
                        "aktorId",
                        "01234567891",
                        null,
                        null,
                        Adresse(null, null, null, null, null),
                        null,
                    ),
                kontaktMedPasient =
                    KontaktMedPasient(
                        LocalDate.now(),
                        "Begrunnelse",
                    ),
                utdypendeOpplysninger = emptyMap(),
                msgId = "msgid",
                pasientAktoerId = "aktorId",
                avsenderSystem = AvsenderSystem("Navn", "verjosn"),
                perioder =
                    periodeListe
                        ?: listOf(
                            Periode(
                                fom = LocalDate.now().minusMonths(1),
                                tom = LocalDate.now().minusWeeks(3),
                                aktivitetIkkeMulig =
                                    AktivitetIkkeMulig(MedisinskArsak(null, emptyList()), null),
                                avventendeInnspillTilArbeidsgiver = null,
                                behandlingsdager = 0,
                                gradert = null,
                                reisetilskudd = false,
                            ),
                        ),
                signaturDato = LocalDateTime.now(),
            ),
        status =
            StatusDbModel(
                "SENDT",
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(7),
                ArbeidsgiverDbModel("9898", null, "Bedriften AS"),
            ),
        merknader = null,
        utenlandskSykmelding = null,
    )
}

fun getNarmesteLeder(): NarmesteLeder {
    return NarmesteLeder(
        fnr = "12345678912",
        narmesteLederFnr = "12345",
        orgnummer = "9898",
        narmesteLederTelefonnummer = "90909090",
        narmesteLederEpost = "mail@nav.no",
        aktivFom = LocalDate.of(2019, 2, 2),
        aktivTom = null,
        arbeidsgiverForskutterer = true,
    )
}

fun getExpectedNarmestelederResponse(): NlResponse {
    return NlResponse(
        orgnummer = "9898",
        utbetalesLonn = true,
        leder =
            Leder(
                fnr = "12345",
                mobil = "90909090",
                epost = "mail@nav.no",
                fornavn = null,
                etternavn = null,
            ),
        sykmeldt = Sykmeldt(fnr = "12345678913", navn = null),
        aktivFom = LocalDate.of(2019, 2, 2).atStartOfDay().atOffset(ZoneOffset.UTC),
        aktivTom = null,
    )
}
