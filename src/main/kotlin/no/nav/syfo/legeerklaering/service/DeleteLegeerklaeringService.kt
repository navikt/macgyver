package no.nav.syfo.legeerklaering.service

import no.nav.syfo.log
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

class DeleteLegeerklaeringService(
    val tombstoneProducer: KafkaProducer<String, Any?>,
    val topics: List<String>,

) {
    fun deleteLegeerklaering(legeerklaeringId: String) {
        try {

            topics.forEach { topic ->
                tombstoneProducer.send(ProducerRecord(topic, legeerklaeringId, null)).get()
            }
        } catch (exception: Exception) {
            log.error(
                "Noe gikk galt med sletting av legeerklæring, legeerklaeringId $legeerklaeringId: {}",
                exception.message
            )
            throw exception
        }
    }
}
