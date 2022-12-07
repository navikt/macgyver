package no.nav.syfo.sykmelding.gamlesykmeldinger

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import no.nav.syfo.db.DatabaseInterface
import no.nav.syfo.log
import no.nav.syfo.sykmelding.gamlesykmeldinger.db.getGamleSykmeldingIds
import no.nav.syfo.sykmelding.gamlesykmeldinger.kafka.SykmeldingIdKafkaProducer

class GamleSykmeldingerService(
    private val db: DatabaseInterface,
    private val sykmeldingIdKafkaProducer: SykmeldingIdKafkaProducer

) {
    @OptIn(DelicateCoroutinesApi::class)
    fun getGamleSykmeldingIdsAndWriteToTopic() {
        GlobalScope.launch(Dispatchers.IO) {
            log.info("Starting job")
            val sykmeldingIds = db.connection.getGamleSykmeldingIds()
            log.info("got ${sykmeldingIds.size} from DB")
            sykmeldingIds.forEach { sykmeldingIdKafkaProducer.send(it) }
        }
    }
}
