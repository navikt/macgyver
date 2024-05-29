package no.nav.syfo.legeerklaering

import no.nav.syfo.sykmelding.delete_sykmelding.TombstoneKafkaProducer

class DeleteLegeerklaeringService(
    val tombstoneProducer: TombstoneKafkaProducer,
    val topics: List<String>,
) {
    fun deleteLegeerklaering(legeerklaeringId: String) {
        tombstoneProducer.send(topics, legeerklaeringId)
    }
}
