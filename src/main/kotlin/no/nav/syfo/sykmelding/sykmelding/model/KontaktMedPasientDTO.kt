package no.nav.syfo.sykmelding.sykmelding.model

import java.time.LocalDate

data class KontaktMedPasientDTO(
    val kontaktDato: LocalDate?,
    val begrunnelseIkkeKontakt: String?
)
