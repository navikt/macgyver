package no.nav.syfo.identendring.db

import no.nav.syfo.model.UtenlandskSykmelding
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

data class ArbeidsgiverDbModel(
    val orgnummer: String,
    val juridiskOrgnummer: String?,
    val orgNavn: String

)

data class StatusDbModel(
    val statusEvent: String,
    val statusTimestamp: OffsetDateTime,
    val arbeidsgiver: ArbeidsgiverDbModel?
)

data class SykmeldingDbModelUtenBehandlingsutfall(
    val id: String,
    val mottattTidspunkt: OffsetDateTime,
    val legekontorOrgNr: String?,
    val sykmeldingsDokument: Sykmelding,
    val status: StatusDbModel,
    val merknader: List<Merknad>?,
    val utenlandskSykmelding: UtenlandskSykmelding?
)

data class Sykmelding(
    val id: String,
    val msgId: String,
    val pasientAktoerId: String,
    val medisinskVurdering: MedisinskVurdering,
    val skjermesForPasient: Boolean,
    val arbeidsgiver: Arbeidsgiver,
    val perioder: List<Periode>,
    val prognose: Prognose?,
    val utdypendeOpplysninger: Map<String, Map<String, SporsmalSvar>>,
    val tiltakArbeidsplassen: String?,
    val tiltakNAV: String?,
    val andreTiltak: String?,
    val meldingTilNAV: MeldingTilNAV?,
    val meldingTilArbeidsgiver: String?,
    val kontaktMedPasient: KontaktMedPasient,
    val behandletTidspunkt: LocalDateTime,
    val behandler: Behandler,
    val avsenderSystem: AvsenderSystem,
    val syketilfelleStartDato: LocalDate?,
    val signaturDato: LocalDateTime,
    val navnFastlege: String?
)

data class MedisinskVurdering(
    val hovedDiagnose: Diagnose?,
    val biDiagnoser: List<Diagnose>,
    val svangerskap: Boolean,
    val yrkesskade: Boolean,
    val yrkesskadeDato: LocalDate?,
    val annenFraversArsak: AnnenFraversArsak?
)

data class Diagnose(
    val system: String,
    val kode: String,
    val tekst: String?
)

data class AnnenFraversArsak(
    val beskrivelse: String?,
    val grunn: List<AnnenFraverGrunn>
)

data class Arbeidsgiver(
    val harArbeidsgiver: HarArbeidsgiver,
    val navn: String?,
    val yrkesbetegnelse: String?,
    val stillingsprosent: Int?
)

enum class HarArbeidsgiver(val codeValue: String, val text: String, val oid: String = "2.16.578.1.12.4.1.1.8130") {
    EN_ARBEIDSGIVER("1", "??n arbeidsgiver"),
    FLERE_ARBEIDSGIVERE("2", "Flere arbeidsgivere"),
    INGEN_ARBEIDSGIVER("3", "Ingen arbeidsgiver")
}

data class Periode(
    val fom: LocalDate,
    val tom: LocalDate,
    val aktivitetIkkeMulig: AktivitetIkkeMulig?,
    val avventendeInnspillTilArbeidsgiver: String?,
    val behandlingsdager: Int?,
    val gradert: Gradert?,
    val reisetilskudd: Boolean
)

data class AktivitetIkkeMulig(
    val medisinskArsak: MedisinskArsak?,
    val arbeidsrelatertArsak: ArbeidsrelatertArsak?
)

data class ArbeidsrelatertArsak(
    val beskrivelse: String?,
    val arsak: List<ArbeidsrelatertArsakType>
)

data class MedisinskArsak(
    val beskrivelse: String?,
    val arsak: List<MedisinskArsakType>
)

enum class ArbeidsrelatertArsakType(val codeValue: String, val text: String, val oid: String = "2.16.578.1.12.4.1.1.8132") {
    MANGLENDE_TILRETTELEGGING("1", "Manglende tilrettelegging p?? arbeidsplassen"),
    ANNET("9", "Annet")
}

enum class MedisinskArsakType(val codeValue: String, val text: String, val oid: String = "2.16.578.1.12.4.1.1.8133") {
    TILSTAND_HINDRER_AKTIVITET("1", "Helsetilstanden hindrer pasienten i ?? v??re i aktivitet"),
    AKTIVITET_FORVERRER_TILSTAND("2", "Aktivitet vil forverre helsetilstanden"),
    AKTIVITET_FORHINDRER_BEDRING("3", "Aktivitet vil hindre/forsinke bedring av helsetilstanden"),
    ANNET("9", "Annet")
}

data class Gradert(
    val reisetilskudd: Boolean,
    val grad: Int
)

data class Prognose(
    val arbeidsforEtterPeriode: Boolean,
    val hensynArbeidsplassen: String?,
    val erIArbeid: ErIArbeid?,
    val erIkkeIArbeid: ErIkkeIArbeid?
)

data class ErIArbeid(
    val egetArbeidPaSikt: Boolean,
    val annetArbeidPaSikt: Boolean,
    val arbeidFOM: LocalDate?,
    val vurderingsdato: LocalDate?
)

data class ErIkkeIArbeid(
    val arbeidsforPaSikt: Boolean,
    val arbeidsforFOM: LocalDate?,
    val vurderingsdato: LocalDate?
)

data class MeldingTilNAV(
    val bistandUmiddelbart: Boolean,
    val beskrivBistand: String?
)

data class KontaktMedPasient(
    val kontaktDato: LocalDate?,
    val begrunnelseIkkeKontakt: String?
)

data class Behandler(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val aktoerId: String,
    val fnr: String,
    val hpr: String?,
    val her: String?,
    val adresse: Adresse,
    val tlf: String?
)

data class Adresse(
    val gate: String?,
    val postnummer: Int?,
    val kommune: String?,
    val postboks: String?,
    val land: String?
)

data class AvsenderSystem(
    val navn: String,
    val versjon: String
)

data class SporsmalSvar(
    val sporsmal: String?,
    val svar: String,
    val restriksjoner: List<SvarRestriksjon>
)

enum class SvarRestriksjon(val codeValue: String, val text: String, val oid: String = "2.16.578.1.12.4.1.1.8134") {
    SKJERMET_FOR_ARBEIDSGIVER("A", "Informasjonen skal ikke vises arbeidsgiver"),
    SKJERMET_FOR_PASIENT("P", "Informasjonen skal ikke vises pasient"),
    SKJERMET_FOR_NAV("N", "Informasjonen skal ikke vises NAV")
}

enum class AnnenFraverGrunn(val codeValue: String, val text: String, val oid: String = "2.16.578.1.12.4.1.1.8131") {
    GODKJENT_HELSEINSTITUSJON("1", "N??r vedkommende er innlagt i en godkjent helseinstitusjon"),
    BEHANDLING_FORHINDRER_ARBEID("2", "N??r vedkommende er under behandling og legen erkl??rer at behandlingen gj??r det n??dvendig at vedkommende ikke arbeider"),
    ARBEIDSRETTET_TILTAK("3", "N??r vedkommende deltar p?? et arbeidsrettet tiltak"),
    MOTTAR_TILSKUDD_GRUNNET_HELSETILSTAND("4", "N??r vedkommende p?? grunn av sykdom, skade eller lyte f??r tilskott n??r vedkommende p?? grunn av sykdom, skade eller lyte f??r tilskott"),
    NODVENDIG_KONTROLLUNDENRSOKELSE("5", "N??r vedkommende er til n??dvendig kontrollunders??kelse som krever minst 24 timers frav??r, reisetid medregnet"),
    SMITTEFARE("6", "N??r vedkommende myndighet har nedlagt forbud mot at han eller hun arbeider p?? grunn av smittefare"),
    ABORT("7", "N??r vedkommende er arbeidsuf??r som f??lge av svangerskapsavbrudd"),
    UFOR_GRUNNET_BARNLOSHET("8", "N??r vedkommende er arbeidsuf??r som f??lge av behandling for barnl??shet"),
    DONOR("9", "N??r vedkommende er donor eller er under vurdering som donor"),
    BEHANDLING_STERILISERING("10", "N??r vedkommende er arbeidsuf??r som f??lge av behandling i forbindelse med sterilisering")
}

data class Merknad(
    val type: String,
    val beskrivelse: String?
)

enum class StatusEvent {
    APEN, AVBRUTT, UTGATT, SENDT, BEKREFTET, SLETTET
}
