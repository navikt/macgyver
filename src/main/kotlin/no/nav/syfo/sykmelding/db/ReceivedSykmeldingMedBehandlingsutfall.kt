package no.nav.syfo.sykmelding.db

import no.nav.syfo.model.Behandlingsutfall
import no.nav.syfo.model.ReceivedSykmelding

data class ReceivedSykmeldingMedBehandlingsutfall(
    val receivedSykmelding: ReceivedSykmelding,
    val behandlingsutfall: Behandlingsutfall
)
