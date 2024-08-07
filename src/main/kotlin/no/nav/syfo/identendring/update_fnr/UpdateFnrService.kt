package no.nav.syfo.identendring.update_fnr

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import no.nav.syfo.identendring.model.toArbeidsgiverSykmelding
import no.nav.syfo.logging.logger
import no.nav.syfo.model.sykmeldingstatus.ArbeidsgiverStatusDTO
import no.nav.syfo.model.sykmeldingstatus.KafkaMetadataDTO
import no.nav.syfo.model.sykmeldingstatus.STATUS_SENDT
import no.nav.syfo.model.sykmeldingstatus.ShortNameDTO
import no.nav.syfo.model.sykmeldingstatus.SporsmalOgSvarDTO
import no.nav.syfo.model.sykmeldingstatus.SvartypeDTO
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO
import no.nav.syfo.narmesteleder.NarmesteLederResponseKafkaProducer
import no.nav.syfo.narmesteleder.NarmestelederClient
import no.nav.syfo.narmesteleder.kafkamodel.KafkaMetadata
import no.nav.syfo.narmesteleder.kafkamodel.Leder
import no.nav.syfo.narmesteleder.kafkamodel.NlAvbrutt
import no.nav.syfo.narmesteleder.kafkamodel.NlResponse
import no.nav.syfo.narmesteleder.kafkamodel.NlResponseKafkaMessage
import no.nav.syfo.narmesteleder.kafkamodel.Sykmeldt
import no.nav.syfo.pdl.PdlPersonService
import no.nav.syfo.sykmelding.aivenmigrering.SykmeldingV2KafkaMessage
import no.nav.syfo.sykmelding.aivenmigrering.SykmeldingV2KafkaProducer

class UpdateFnrService(
    private val pdlPersonService: PdlPersonService,
    private val updateFnrDatabase: UpdateFnrDatabase,
    private val sendtSykmeldingKafkaProducer: SykmeldingV2KafkaProducer,
    private val narmesteLederResponseKafkaProducer: NarmesteLederResponseKafkaProducer,
    private val narmestelederClient: NarmestelederClient,
    private val sendtSykmeldingTopic: String,
) {

    suspend fun updateNlFnr(fnr: String, nyttFnr: String): Boolean {
        val pdlPerson = pdlPersonService.getPdlPerson(fnr)
        when {
            pdlPerson.fnr != nyttFnr -> {
                val msg =
                    "Oppdatering av leders fnr feilet, nyttFnr står ikke som aktivt fnr for aktøren i PDL"
                logger.error(msg)
                throw UpdateIdentException(msg)
            }
            !pdlPerson.harHistoriskFnr(fnr) -> {
                val msg = "Oppdatering av leders fnr feilet, fnr er ikke historisk for aktør"
                logger.error(msg)
                throw UpdateIdentException(msg)
            }
            else -> {
                logger.info("Oppdaterer fnr for leder")
                val aktiveNlKoblinger = narmestelederClient.getNarmestelederKoblingerForLeder(fnr)
                logger.info("Bryter og gjenoppretter ${aktiveNlKoblinger.size} nl-koblinger")
                aktiveNlKoblinger.forEach {
                    narmesteLederResponseKafkaProducer.publishToKafka(
                        NlResponseKafkaMessage(
                            kafkaMetadata =
                                KafkaMetadata(OffsetDateTime.now(ZoneOffset.UTC), "macgyver"),
                            nlResponse = null,
                            nlAvbrutt =
                                NlAvbrutt(
                                    orgnummer = it.orgnummer,
                                    sykmeldtFnr = it.fnr,
                                    aktivTom = OffsetDateTime.now(ZoneOffset.UTC),
                                ),
                        ),
                        it.orgnummer,
                    )
                    narmesteLederResponseKafkaProducer.publishToKafka(
                        NlResponseKafkaMessage(
                            kafkaMetadata =
                                KafkaMetadata(OffsetDateTime.now(ZoneOffset.UTC), "macgyver"),
                            nlResponse =
                                NlResponse(
                                    orgnummer = it.orgnummer,
                                    utbetalesLonn = it.arbeidsgiverForskutterer,
                                    leder =
                                        Leder(
                                            fnr = nyttFnr,
                                            mobil = it.narmesteLederTelefonnummer,
                                            epost = it.narmesteLederEpost,
                                            fornavn = null,
                                            etternavn = null,
                                        ),
                                    sykmeldt =
                                        Sykmeldt(
                                            fnr = it.fnr,
                                            navn = null,
                                        ),
                                    aktivFom = it.aktivFom.atStartOfDay().atOffset(ZoneOffset.UTC),
                                    aktivTom = null,
                                ),
                        ),
                        it.orgnummer,
                    )
                }
                logger.info("Alle aktive nl-koblinger er oppdatert")
                return aktiveNlKoblinger.isNotEmpty()
            }
        }
    }

    suspend fun updateFnr(fnr: String, nyttFnr: String): Boolean {
        val pdlPerson = pdlPersonService.getPdlPerson(fnr)

        when {
            pdlPerson.fnr != nyttFnr -> {
                val msg =
                    "Oppdatering av fnr feilet, nyttFnr står ikke som aktivt fnr for aktøren i PDL"
                logger.error(msg)
                throw UpdateIdentException(msg)
            }
            !pdlPerson.harHistoriskFnr(fnr) -> {
                val msg = "Oppdatering av fnr feilet, fnr er ikke historisk for aktør"
                logger.error(msg)
                throw UpdateIdentException(msg)
            }
            else -> {
                logger.info("Oppdaterer fnr for person")
                val sykmeldinger = updateFnrDatabase.getSykmeldingerMedFnrUtenBehandlingsutfall(fnr)
                val sendteSykmeldingerSisteFireMnd =
                    sykmeldinger.filter {
                        it.status.statusEvent == STATUS_SENDT &&
                            finnSisteTom(it.sykmeldingsDokument.perioder)
                                .isAfter(LocalDate.now().minusMonths(4))
                    }
                val aktiveNarmesteledere =
                    narmestelederClient.getNarmesteledere(fnr).filter { it.aktivTom == null }
                logger.info("Resender ${sendteSykmeldingerSisteFireMnd.size} sendte sykmeldinger")
                sendteSykmeldingerSisteFireMnd.forEach {
                    sendtSykmeldingKafkaProducer.sendSykmelding(
                        sykmeldingKafkaMessage = getKafkaMessage(it, nyttFnr),
                        sykmeldingId = it.id,
                        topic = sendtSykmeldingTopic,
                    )
                }
                logger.info("Bryter og gjenoppretter ${aktiveNarmesteledere.size} nl-koblinger")
                aktiveNarmesteledere.forEach {
                    narmesteLederResponseKafkaProducer.publishToKafka(
                        NlResponseKafkaMessage(
                            kafkaMetadata =
                                KafkaMetadata(OffsetDateTime.now(ZoneOffset.UTC), "macgyver"),
                            nlResponse = null,
                            nlAvbrutt =
                                NlAvbrutt(
                                    orgnummer = it.orgnummer,
                                    sykmeldtFnr = fnr,
                                    aktivTom = OffsetDateTime.now(ZoneOffset.UTC),
                                ),
                        ),
                        it.orgnummer,
                    )
                    narmesteLederResponseKafkaProducer.publishToKafka(
                        NlResponseKafkaMessage(
                            kafkaMetadata =
                                KafkaMetadata(OffsetDateTime.now(ZoneOffset.UTC), "macgyver"),
                            nlResponse =
                                NlResponse(
                                    orgnummer = it.orgnummer,
                                    utbetalesLonn = it.arbeidsgiverForskutterer,
                                    leder =
                                        Leder(
                                            fnr = it.narmesteLederFnr,
                                            mobil = it.narmesteLederTelefonnummer,
                                            epost = it.narmesteLederEpost,
                                            fornavn = null,
                                            etternavn = null,
                                        ),
                                    sykmeldt =
                                        Sykmeldt(
                                            fnr = nyttFnr,
                                            navn = null,
                                        ),
                                    aktivFom = it.aktivFom.atStartOfDay().atOffset(ZoneOffset.UTC),
                                    aktivTom = null,
                                ),
                        ),
                        it.orgnummer,
                    )
                }
                logger.info("Oppdaterer register-databasen")
                val updateFnr = updateFnrDatabase.updateFnr(nyttFnr = nyttFnr, fnr = fnr)
                return updateFnr > 0
            }
        }
    }
}

private fun finnSisteTom(perioder: List<Periode>): LocalDate {
    return perioder.maxByOrNull { it.tom }?.tom
        ?: throw IllegalStateException("Skal ikke kunne ha periode uten tom")
}

private fun getKafkaMessage(
    sykmelding: SykmeldingDbModelUtenBehandlingsutfall,
    nyttFnr: String
): SykmeldingV2KafkaMessage {
    val sendtSykmelding = sykmelding.toArbeidsgiverSykmelding()
    val metadata =
        KafkaMetadataDTO(
            sykmeldingId = sykmelding.id,
            timestamp = sykmelding.status.statusTimestamp,
            source = "macgyver",
            fnr = nyttFnr,
        )
    val sendEvent =
        SykmeldingStatusKafkaEventDTO(
            metadata.sykmeldingId,
            metadata.timestamp,
            STATUS_SENDT,
            ArbeidsgiverStatusDTO(
                sykmelding.status.arbeidsgiver!!.orgnummer,
                sykmelding.status.arbeidsgiver.juridiskOrgnummer,
                sykmelding.status.arbeidsgiver.orgNavn,
            ),
            listOf(
                SporsmalOgSvarDTO(
                    tekst = "Jeg er sykmeldt fra",
                    shortName = ShortNameDTO.ARBEIDSSITUASJON,
                    svartype = SvartypeDTO.ARBEIDSSITUASJON,
                    svar = "ARBEIDSTAKER",
                ),
            ),
        )
    return SykmeldingV2KafkaMessage(sendtSykmelding, metadata, sendEvent)
}

class UpdateIdentException(override val message: String) : Exception(message)
