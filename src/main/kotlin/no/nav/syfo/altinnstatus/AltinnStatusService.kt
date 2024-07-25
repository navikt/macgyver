package no.nav.syfo.altinnstatus

class AltinnStatusService {
    fun getAltinnStatus(sykmeldingId: String, token: Any?): AltinnStatus {

        // må ha ein klient som gjer kall mot syfosmaltinn apiet.


        // TODO replace with actual data from Altinn
        return AltinnStatus(
            correspondenceId = "123",
            createdDate = "2021-01-01",
            orgnummer = "123456789",
            sendersReference = "123",
            statusChanges = listOf(
                StatusChange(
                    date = "2021-01-01",
                    type = "SENDT"
                )
            )
        )
    }


}
