package no.nav.syfo.model.sykmeldingstatus

import java.time.OffsetDateTime
import no.nav.syfo.model.sykmelding.model.TidligereArbeidsgiverDTO

data class SykmeldingStatusKafkaEventDTO(
    val sykmeldingId: String,
    val timestamp: OffsetDateTime,
    val statusEvent: String,
    val arbeidsgiver: ArbeidsgiverStatusDTO? = null,
    val sporsmals: List<SporsmalOgSvarDTO>? = null,
    val erSvarOppdatering: Boolean? = null,
    val tidligereArbeidsgiver: TidligereArbeidsgiverDTO? = null,
)
