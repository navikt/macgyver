package no.nav.syfo.infotrygd

import no.nav.syfo.model.Diagnose
import java.time.LocalDate
import java.util.UUID

data class InfotrygdQuery(
    val ident: String,
    val fom: LocalDate?,
    val tom: LocalDate?,
    val hoveddiagnose: Diagnose?,
    val bidiagnose: Diagnose?,
    val tknumber: String?,
    val fodselsnrBehandler: String?,
    val traceId: String = UUID.randomUUID().toString(),
)


data class InfotrygdResponse(
    val identDato: String?,
    val tkNummer: String?,
)
