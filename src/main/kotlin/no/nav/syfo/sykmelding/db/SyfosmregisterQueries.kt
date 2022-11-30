package no.nav.syfo.sykmelding.db

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.syfo.db.Database
import no.nav.syfo.db.toList
import no.nav.syfo.model.Behandlingsutfall
import no.nav.syfo.model.Merknad
import no.nav.syfo.model.ReceivedSykmelding
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.model.UtenlandskSykmelding
import no.nav.syfo.objectMapper
import java.sql.ResultSet

fun Database.getSykmelding(id: String): ReceivedSykmeldingMedBehandlingsutfall? {
    return connection.use { connection ->
        connection.prepareStatement(
            """
                    SELECT opplysninger.id,
                    mottatt_tidspunkt,
                    behandlingsutfall,
                    legekontor_org_nr,
                    sykmelding,
                    status.event,
                    status.timestamp,
                    arbeidsgiver.orgnummer,
                    arbeidsgiver.juridisk_orgnummer,
                    arbeidsgiver.navn,
                    merknader,
                    utenlandsk_sykmelding
                    FROM sykmeldingsopplysninger AS opplysninger
                        INNER JOIN sykmeldingsdokument AS dokument ON opplysninger.id = dokument.id
                        INNER JOIN behandlingsutfall AS utfall ON opplysninger.id = utfall.id
                        LEFT OUTER JOIN arbeidsgiver as arbeidsgiver on arbeidsgiver.sykmelding_id = opplysninger.id
                        LEFT OUTER JOIN sykmeldingstatus AS status ON opplysninger.id = status.sykmelding_id AND
                                                                   status.timestamp = (SELECT timestamp
                                                                                             FROM sykmeldingstatus
                                                                                             WHERE sykmelding_id = opplysninger.id
                                                                                             ORDER BY timestamp DESC
                                                                                             LIMIT 1)
                    where opplysninger.id = ?
                    and not exists(select 1 from sykmeldingstatus where sykmelding_id = opplysninger.id and event in ('SLETTET'));
                    """
        ).use {
            it.setString(1, id)
            it.executeQuery().toList { toReceivedSykmeldingMedBehandlingsutfall() }.firstOrNull()
        }
    }
}

fun ResultSet.toReceivedSykmeldingMedBehandlingsutfall(): ReceivedSykmeldingMedBehandlingsutfall {
    val sykmelding = objectMapper.readValue(getString("sykmelding"), Sykmelding::class.java)
    return ReceivedSykmeldingMedBehandlingsutfall(
        receivedSykmelding = ReceivedSykmelding(
            sykmelding = sykmelding,
            personNrPasient = getString("pasient_fnr"),
            tlfPasient = "",
            personNrLege = getString("lege_fnr"),
            legeHelsepersonellkategori = getString("lege_helsepersonellkategori"),
            legeHprNr = getString("lege_hpr"),
            navLogId = getString("mottak_id"),
            msgId = sykmelding.msgId,
            legekontorOrgNr = getString("legekontor_org_nr"),
            legekontorHerId = getString("legekontor_her_id"),
            legekontorReshId = getString("legekontor_resh_id"),
            legekontorOrgName = "",
            mottattDato = getTimestamp("mottatt_tidspunkt").toLocalDateTime(),
            rulesetVersion = null,
            merknader = getString("merknader")?.let { objectMapper.readValue<List<Merknad>>(it) },
            utenlandskSykmelding = getString("utenlandsk_sykmelding")?.let { objectMapper.readValue<UtenlandskSykmelding>(it) },
            partnerreferanse = getString("partnerreferanse"),
            vedlegg = emptyList(),
            fellesformat = "",
            tssid = getString("tss_id")
        ),
        behandlingsutfall = objectMapper.readValue<Behandlingsutfall>(getString("behandlingsutfall"))
    )
}
