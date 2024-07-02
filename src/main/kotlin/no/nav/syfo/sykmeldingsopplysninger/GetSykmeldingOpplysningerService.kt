package no.nav.syfo.sykmeldingsopplysninger

class GetSykmeldingOpplysningerService(
    val getSykmeldingOpplysningerDatabase: GetSykmeldingOpplysningerDatabase
) {

    suspend fun getSykmeldingOpplysninger(fnr: String) : List<Sykmelding> {
        return getSykmeldingOpplysningerDatabase.getAlleSykmeldinger(fnr)
    }
}
