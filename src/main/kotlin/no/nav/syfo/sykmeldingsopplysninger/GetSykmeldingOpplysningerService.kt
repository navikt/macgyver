package no.nav.syfo.sykmeldingsopplysninger

class GetSykmeldingOpplysningerService(
    val getSykmeldingOpplysningerDatabase: GetSykmeldingOpplysningerDatabase
) {

    suspend fun getSykmeldingOpplysninger(fnr: String): Sykmeldingsopplysninger {
        return Sykmeldingsopplysninger(
            fnr,
            getSykmeldingOpplysningerDatabase.getAlleSykmeldinger(fnr)
        )
    }

    suspend fun getFnrFromSykmeldingId(sykmeldingId: String?): String? {
        if(sykmeldingId == null) return null
        return getSykmeldingOpplysningerDatabase.getFnrForSykmeldingId(sykmeldingId)
    }
}
