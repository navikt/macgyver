package no.nav.syfo.sykmeldingsopplysninger

import java.time.LocalDate
import java.util.UUID
import no.nav.syfo.model.Merknad
import no.nav.syfo.model.RuleInfo
import no.nav.syfo.model.Status

interface SykmeldingsOpplysningerClient {
    suspend fun getSykmeldingsopplysninger(fnr: String): Sykmeldingsopplysninger
}

class ProductionSykmeldingsOpplysningerClient() : SykmeldingsOpplysningerClient {
    override suspend fun getSykmeldingsopplysninger(fnr: String): Sykmeldingsopplysninger {
        TODO("Not yet implemented")
    }
}

class DevelopmentSykmeldingsOpplysningerClient() : SykmeldingsOpplysningerClient {
    override suspend fun getSykmeldingsopplysninger(fnr: String): Sykmeldingsopplysninger {
        return Sykmeldingsopplysninger(
            fnr = "12345678910",
            sykmeldingId = UUID.randomUUID().toString(),
            merknader = listOf(Merknad(type = "Merknadtype", beskrivelse = "beskrivelse")),
            tssId = "123456",
            statusEvent = "SENDT",
            mottakId = UUID.randomUUID().toString(),
            mottattTidspunkt = LocalDate.parse("2021-01-01").atStartOfDay(),
            behandlingsUtfall =
                BehandlingsUtfall(
                    status = "OK",
                    ruleHits =
                        listOf(
                            RuleInfo(
                                ruleName = "ruleName",
                                messageForSender = "messageForSender",
                                messageForUser = "messageForUser",
                                ruleStatus = Status.OK
                            )
                        )
                ),
            perioder =
                listOf(
                    Periode(
                        fom = LocalDate.of(2021, 1, 1),
                        tom = LocalDate.of(2021, 1, 30),
                        aktivitetIkkeMulig = null,
                        avventendeInnspillTilArbeidsgiver = null,
                        behandlingsdager = 1,
                        gradert = null,
                        reisetilskudd = false
                    )
                )
        )
    }
}
