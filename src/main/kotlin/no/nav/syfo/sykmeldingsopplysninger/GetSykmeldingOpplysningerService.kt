package no.nav.syfo.sykmeldingsopplysninger

class GetSykmeldingOpplysningerService(
    val getSykmeldingOpplysningerDatabase: GetSykmeldingOpplysningerDatabase
) {

    suspend fun getSykmeldingOpplysninger(fnr: String) : Sykmeldingsopplysninger {
        return Sykmeldingsopplysninger(fnr, getSykmeldingOpplysningerDatabase.getAlleSykmeldinger(fnr))
    }
}
