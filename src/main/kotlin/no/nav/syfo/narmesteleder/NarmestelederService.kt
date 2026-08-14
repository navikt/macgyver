package no.nav.syfo.narmesteleder

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.syfo.logging.logger
import no.nav.syfo.narmesteleder.kafkamodel.KafkaMetadata
import no.nav.syfo.narmesteleder.kafkamodel.NlAvbrutt
import no.nav.syfo.narmesteleder.kafkamodel.NlResponseKafkaMessage
import java.time.OffsetDateTime
import java.time.ZoneOffset

class NarmestelederService(
    private val narmestelederClient: NarmestelederClient,
    private val narmestelederResponseProducer: NarmesteLederResponseKafkaProducer,
) {
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
