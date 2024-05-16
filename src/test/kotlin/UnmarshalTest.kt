import java.io.StringReader
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import no.nav.helse.sm2013.HelseOpplysningerArbeidsuforhet
import no.nav.syfo.utils.fellesformatUnmarshaller
import no.nav.syfo.utils.getFileAsString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

internal class UnmarshalTest {
    @Test
    internal fun `Test unmarshal dates testsett 1`() {
        val healthInformation =
            fellesformatUnmarshaller.unmarshal(
                StringReader(getFileAsString("src/test/resources/helseopplysninger-ISO-8859-1.xml"))
            ) as HelseOpplysningerArbeidsuforhet
        val expectedFomDate = LocalDate.of(2017, 9, 1)
        val expectedTomDate = LocalDate.of(2017, 10, 27)
        assertEquals(expectedFomDate, healthInformation.aktivitet.periode.first().periodeFOMDato)
        assertEquals(expectedTomDate, healthInformation.aktivitet.periode.first().periodeTOMDato)
    }

    @Test
    internal fun `Test timestamp`() {
        val string = Timestamp.valueOf(LocalDateTime.now()).toString()
        val r = Timestamp.valueOf(string).toLocalDateTime()
        assertNotEquals(r, null)
    }
}
