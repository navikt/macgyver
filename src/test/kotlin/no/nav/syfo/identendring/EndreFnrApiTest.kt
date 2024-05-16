package no.nav.syfo.identendring

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import no.nav.syfo.db.Database
import no.nav.syfo.identendring.api.registerFnrApi
import no.nav.syfo.identendring.client.NarmestelederClient
import no.nav.syfo.identendring.db.updateFnr
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.narmesteleder.NarmesteLederResponseKafkaProducer
import no.nav.syfo.pdl.client.model.IdentInformasjon
import no.nav.syfo.pdl.model.PdlPerson
import no.nav.syfo.pdl.service.PdlPersonService
import no.nav.syfo.sykmelding.aivenmigrering.SykmeldingV2KafkaProducer
import no.nav.syfo.sykmelding.api.model.EndreFnr
import no.nav.syfo.utils.generateJWT
import no.nav.syfo.utils.objectMapper
import no.nav.syfo.utils.setupTestApplication
import no.nav.syfo.utils.testClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin

internal class EndreFnrApiTest {

    @AfterEach fun cleanup() = stopKoin()

    @Test
    internal fun `Test endre fnr`() = testApplication {
        val pdlPersonService = mockk<PdlPersonService>(relaxed = true)
        val sendtSykmeldingKafkaProducer = mockk<SykmeldingV2KafkaProducer>(relaxed = true)
        val narmesteLederResponseKafkaProducer =
            mockk<NarmesteLederResponseKafkaProducer>(relaxed = true)
        val narmestelederClient = mockk<NarmestelederClient>()

        mockkStatic("no.nav.syfo.identendring.db.SyfoSmRegisterKt")
        val db = mockk<Database>(relaxed = true)

        setupTestApplication(withAuth = true)

        routing {
            registerFnrApi(
                UpdateFnrService(
                    pdlPersonService,
                    db,
                    sendtSykmeldingKafkaProducer,
                    narmesteLederResponseKafkaProducer,
                    narmestelederClient,
                    "topic",
                ),
            )
        }

        coEvery { pdlPersonService.getPdlPerson(any()) } returns
            PdlPerson(
                listOf(
                    IdentInformasjon("12345678913", false, "FOLKEREGISTERIDENT"),
                    IdentInformasjon("12345678912", true, "FOLKEREGISTERIDENT"),
                    IdentInformasjon("12345", false, "AKTORID"),
                ),
                "navn navn",
            )
        coEvery { narmestelederClient.getNarmesteledere(any()) } returns emptyList()

        every { db.updateFnr(any(), any()) } returns 1

        val endreFnr = EndreFnr(fnr = "12345678912", nyttFnr = "12345678913")

        val response =
            testClient().post("/api/sykmelding/fnr") {
                headers {
                    append("Content-Type", "application/json")
                    append(HttpHeaders.Authorization, "Bearer ${generateJWT("2", "clientId")}")
                }
                setBody(objectMapper.writeValueAsString(endreFnr))
            }
        val result = response.body<HttpMessage>()

        assertEquals(response.status, HttpStatusCode.OK)
        assertEquals(result, HttpMessage("Vellykket oppdatering."))
    }
}
