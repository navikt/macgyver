package no.nav.syfo.sykmeldingsopplysninger

class GetSykmeldingOpplysningerService(
    val getSykmeldingOpplysningerDatabase: GetSykmeldingOpplysningerDatabase
) {

    suspend fun getSykmeldingOpplysninger(fnr: String) {
        val hentSykmeldingsopplysninger = getSykmeldingOpplysningerDatabase.getAlleSykmeldinger(fnr)
    }
}
