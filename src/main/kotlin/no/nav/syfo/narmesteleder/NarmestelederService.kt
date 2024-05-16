package no.nav.syfo.narmesteleder

import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.syfo.identendring.client.NarmesteLeder
import no.nav.syfo.identendring.client.NarmestelederClient
import no.nav.syfo.narmesteleder.api.NlRequestDTO
import no.nav.syfo.narmesteleder.kafkamodel.NlKafkaMetadata
import no.nav.syfo.narmesteleder.kafkamodel.NlRequest
import no.nav.syfo.narmesteleder.kafkamodel.NlRequestKafkaMessage
import no.nav.syfo.pdl.service.PdlPersonService
import no.nav.syfo.utils.logger
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

class NarmestelederService(
    private val pdlService: PdlPersonService,
    private val narmestelederRequestProducer: KafkaProducer<String, NlRequestKafkaMessage>,
    private val topic: String,
    private val narmestelederClient: NarmestelederClient
) {
    suspend fun sendNewNlRequest(nlRequestDto: NlRequestDTO) =
        withContext(Dispatchers.IO) {
            val person =
                pdlService.getPdlPerson(
                    fnr = nlRequestDto.fnr,
                )
            val nlRequest =
                NlRequestKafkaMessage(
                    nlRequest =
                        NlRequest(
                            requestId = UUID.randomUUID(),
                            sykmeldingId = nlRequestDto.sykmeldingId,
                            fnr = nlRequestDto.fnr,
                            orgnr = nlRequestDto.orgnummer,
                            name = person.navn,
                        ),
                    metadata =
                        NlKafkaMetadata(
                            timestamp = OffsetDateTime.now(ZoneOffset.UTC),
                            source = "macgyver",
                        ),
                )

            narmestelederRequestProducer
                .send(ProducerRecord(topic, nlRequest.nlRequest.orgnr, nlRequest))
                .get()
            logger.info("Sendt nl-request to ${nlRequest.nlRequest.orgnr}")
        }

    suspend fun getNarmesteldereForSykmeldt(sykmeldtFnr: String): List<NarmesteLeder> {
        return narmestelederClient.getNarmesteledere(sykmeldtFnr)
    }
}
