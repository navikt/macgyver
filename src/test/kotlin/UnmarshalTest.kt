
import no.nav.helse.sm2013.HelseOpplysningerArbeidsuforhet
import no.nav.syfo.testutil.getFileAsString
import no.nav.syfo.utils.fellesformatUnmarshaller
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBe
import org.junit.jupiter.api.Test
import java.io.StringReader
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime

internal class UnmarshalTest {
    @Test
    internal fun `Test unmarshal dates testsett 1`() {
        val healthInformation =
            fellesformatUnmarshaller.unmarshal(StringReader(getFileAsString("src/test/resources/helseopplysninger-ISO-8859-1.xml"))) as HelseOpplysningerArbeidsuforhet
        val expectedFomDate = LocalDate.of(2017, 9, 1)
        val expectedTomDate = LocalDate.of(2017, 10, 27)

        expectedFomDate shouldBeEqualTo healthInformation.aktivitet.periode.first().periodeFOMDato
        expectedTomDate shouldBeEqualTo healthInformation.aktivitet.periode.first().periodeTOMDato
    }

    @Test
    internal fun `Test timestamp`() {
        val string = Timestamp.valueOf(LocalDateTime.now()).toString()
        val r = Timestamp.valueOf(string).toLocalDateTime()
        r shouldNotBe null
    }
}
