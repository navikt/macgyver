package no.nav.syfo.narmesteleder

import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.syfo.logging.logger
import no.nav.syfo.narmesteleder.kafkamodel.KafkaMetadata
import no.nav.syfo.narmesteleder.kafkamodel.NlAvbrutt
import no.nav.syfo.narmesteleder.kafkamodel.NlKafkaMetadata
import no.nav.syfo.narmesteleder.kafkamodel.NlRequest
import no.nav.syfo.narmesteleder.kafkamodel.NlRequestKafkaMessage
import no.nav.syfo.narmesteleder.kafkamodel.NlResponseKafkaMessage
import no.nav.syfo.pdl.PdlPersonService

class NarmestelederService(
    private val pdlService: PdlPersonService,
    private val narmestelederRequestProducer: NarmesteLederRequestKafkaProducer,
    private val narmestelederClient: NarmestelederClient,
    private val narmestelederResponseProducer: NarmesteLederResponseKafkaProducer,
) {
    suspend fun sendNewNlRequest(
        narmestelederAltinnRequestPayload: NarmestelederAltinnRequestPayload
    ) =
        withContext(Dispatchers.IO) {
            val person =
                pdlService.getPdlPerson(
                    fnr = narmestelederAltinnRequestPayload.fnr,
                )
            val nlRequest =
                NlRequestKafkaMessage(
                    nlRequest =
                        NlRequest(
                            requestId = UUID.randomUUID(),
                            sykmeldingId = narmestelederAltinnRequestPayload.sykmeldingId,
                            fnr = narmestelederAltinnRequestPayload.fnr,
                            orgnr = narmestelederAltinnRequestPayload.orgnummer,
                            name = person.navn,
                        ),
                    metadata =
                        NlKafkaMetadata(
                            timestamp = OffsetDateTime.now(ZoneOffset.UTC),
                            source = "macgyver",
                        ),
                )
            narmestelederRequestProducer.publishToKafka(nlRequest, nlRequest.nlRequest.orgnr)
            logger.info("Sendt nl-request to ${nlRequest.nlRequest.orgnr}")
        }

    suspend fun deaktiverNarmesteLeder(
        fnr: String, orgnummer: String
    ) =
        withContext(Dispatchers.IO) {
            narmestelederResponseProducer.publishToKafka(
                NlResponseKafkaMessage(
                    kafkaMetadata =
                        KafkaMetadata(OffsetDateTime.now(ZoneOffset.UTC), "macgyver"),
                    nlResponse = null,
                    nlAvbrutt =
                        NlAvbrutt(
                            orgnummer = orgnummer,
                            sykmeldtFnr = fnr,
                            aktivTom = OffsetDateTime.now(ZoneOffset.UTC),
                        ),
                ),
                orgnummer,
            )
           logger.info("Sendt nl-avbrutt to $orgnummer")
        }
    suspend fun getNarmesteldereForSykmeldt(sykmeldtFnr: String): List<NarmesteLeder> {
        return narmestelederClient.getNarmesteledere(sykmeldtFnr)
    }

    suspend fun getNarmestelderKoblingerForLeder(lederFnr: String): List<NarmesteLeder> {
        return narmestelederClient.getNarmestelederKoblingerForLeder(lederFnr)
    }
}
