package no.nav.syfo

import no.nav.syfo.model.Adresse
import no.nav.syfo.model.Arbeidsgiver
import no.nav.syfo.model.AvsenderSystem
import no.nav.syfo.model.Behandler
import no.nav.syfo.model.HarArbeidsgiver
import no.nav.syfo.model.KontaktMedPasient
import no.nav.syfo.model.MedisinskVurdering
import no.nav.syfo.model.ReceivedSykmelding
import no.nav.syfo.model.Sykmelding
import java.time.LocalDate
import java.time.LocalDateTime

fun getReceivedSykmelding(sykmeldingId: String): ReceivedSykmelding =
    ReceivedSykmelding(
        sykmelding = Sykmelding(
            id = sykmeldingId,
            behandletTidspunkt = LocalDateTime.now(),
            behandler = Behandler(
                fornavn = "fornavn",
                adresse = Adresse(null, null, null, null, null),
                fnr = "12345678901",
                etternavn = "etternavn",
                aktoerId = "aktorId",
                her = null,
                tlf = null,
                hpr = null,
                mellomnavn = null
            ),
            arbeidsgiver = Arbeidsgiver(HarArbeidsgiver.EN_ARBEIDSGIVER, null, null, null),
            andreTiltak = null,
            avsenderSystem = AvsenderSystem("avsender", "1"),
            kontaktMedPasient = KontaktMedPasient(LocalDate.now(), null),
            medisinskVurdering = MedisinskVurdering(null, emptyList(), false, false, null, null),
            meldingTilArbeidsgiver = null,
            meldingTilNAV = null,
            msgId = "1",
            navnFastlege = null,
            pasientAktoerId = "1234",
            perioder = emptyList(),
            prognose = null,
            signaturDato = LocalDateTime.now(),
            skjermesForPasient = false,
            syketilfelleStartDato = LocalDate.now(),
            tiltakArbeidsplassen = null,
            tiltakNAV = null,
            utdypendeOpplysninger = emptyMap()
        ),
        msgId = "1",
        fellesformat = "",
        legekontorHerId = null,
        legekontorOrgName = "navn",
        legekontorOrgNr = null,
        legekontorReshId = null,
        mottattDato = LocalDateTime.now(),
        navLogId = "1",
        personNrLege = "12345678901",
        legeHprNr = "123774",
        legeHelsepersonellkategori = "LE",
        personNrPasient = "12345678901",
        rulesetVersion = null,
        tlfPasient = null,
        tssid = null,
        merknader = null,
        partnerreferanse = "123456",
        vedlegg = null,
        utenlandskSykmelding = null
    )
