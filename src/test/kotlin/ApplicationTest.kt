import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
import no.nav.syfo.plugins.ApplicationState
import no.nav.syfo.plugins.configureNaisResources
import no.nav.syfo.utils.setupTestApplication
import no.nav.syfo.utils.testClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import org.koin.dsl.module

internal class ApplicationTest {

    @AfterEach fun cleanup() = stopKoin()

    @Test
    internal fun `Returns ok on is_alive`() = testApplication {
        setupTestApplication()
        application { configureNaisResources() }

        val response = testClient().get("/internal/is_alive")
        val result = response.body<String>()

        assertEquals(response.status, HttpStatusCode.OK)
        assertEquals(result, "I'm alive! :)")
    }

    @Test
    internal fun `Returns ok in is_ready`() = testApplication {
        setupTestApplication()
        application { configureNaisResources() }

        val response = testClient().get("/internal/is_ready")
        val result = response.body<String>()

        assertEquals(response.status, HttpStatusCode.OK)
        assertEquals(result, "I'm ready! :)")
    }

    @Test
    internal fun `Returns internal server error when liveness check fails`() = testApplication {
        setupTestApplication { modules(unreadyApplicationState) }
        application { configureNaisResources() }

        val response = testClient().get("/internal/is_alive")
        val result = response.body<String>()

        assertEquals(response.status, HttpStatusCode.InternalServerError)
        assertEquals(result, "I'm dead x_x")
    }

    @Test
    internal fun `Returns internal server error when readyness check fails`() = testApplication {
        setupTestApplication { modules(unreadyApplicationState) }
        application { configureNaisResources() }

        val response = testClient().get("/internal/is_ready")
        val result = response.body<String>()

        assertEquals(response.status, HttpStatusCode.InternalServerError)
        assertEquals(result, "Please wait! I'm not ready :(")
    }
}

private val unreadyApplicationState = module {
    single {
        val state = ApplicationState()
        state.ready = false
        state.alive = false
        state
    }
}
