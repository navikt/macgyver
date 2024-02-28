package no.nav.syfo.persistering.db.postgres

import no.nav.syfo.model.syfosmregister.Sykmeldingsdokument
import no.nav.syfo.model.syfosmregister.Sykmeldingsopplysninger

data class SykmeldingDbModel(
    val sykmeldingsopplysninger: Sykmeldingsopplysninger,
    val sykmeldingsdokument: Sykmeldingsdokument?,
)
