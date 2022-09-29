package no.nav.syfo.bucket

import com.google.cloud.storage.Storage
import no.nav.syfo.log

class BucketService(
    private val name: String,
    private val storage: Storage
) {

    fun deleteLegeerklaring(objectId: String) {
        val deleteLegeerklaring = storage.delete(name, objectId)
        if (!deleteLegeerklaring) {
            log.error("Feilet å slette legeerklæring fra bucket: $objectId")
            throw RuntimeException("Feilet å slette legeerklæring fra bucket: l $objectId")
        }
    }
}
