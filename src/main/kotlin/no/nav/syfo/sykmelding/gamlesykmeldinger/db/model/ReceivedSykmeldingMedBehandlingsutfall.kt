package no.nav.syfo.sykmelding.gamlesykmeldinger.db.model

import no.nav.syfo.model.Behandlingsutfall
import no.nav.syfo.model.ReceivedSykmelding

data class ReceivedSykmeldingMedBehandlingsutfall(
    val receivedSykmelding: ReceivedSykmelding,
    val behandlingsutfall: Behandlingsutfall
)
