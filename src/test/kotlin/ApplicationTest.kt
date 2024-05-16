import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
import no.nav.syfo.plugins.ApplicationState
import no.nav.syfo.plugins.configureNaisResources
import no.nav.syfo.utils.createTestHttpClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ApplicationTest {

    @Test
    internal fun `Returns ok on is_alive`() = testApplication {
        val client = createTestHttpClient()

        val applicationState = ready()
        application { configureNaisResources(applicationState) }

        val response = client.get("/internal/is_alive")
        val result = response.body<String>()

        assertEquals(response.status, HttpStatusCode.OK)
        assertEquals(result, "I'm alive! :)")
    }

    @Test
    internal fun `Returns ok in is_ready`() = testApplication {
        val client = createTestHttpClient()

        val applicationState = ready()
        application { configureNaisResources(applicationState) }

        val response = client.get("/internal/is_ready")
        val result = response.body<String>()

        assertEquals(response.status, HttpStatusCode.OK)
        assertEquals(result, "I'm ready! :)")
    }

    @Test
    internal fun `Returns internal server error when liveness check fails`() = testApplication {
        val client = createTestHttpClient()

        val applicationState = unready()
        application { configureNaisResources(applicationState) }

        val response = client.get("/internal/is_alive")
        val result = response.body<String>()

        assertEquals(response.status, HttpStatusCode.InternalServerError)
        assertEquals(result, "I'm dead x_x")
    }

    @Test
    internal fun `Returns internal server error when readyness check fails`() = testApplication {
        val client = createTestHttpClient()

        val applicationState = unready()
        application { configureNaisResources(applicationState) }

        val response = client.get("/internal/is_ready")
        val result = response.body<String>()

        assertEquals(response.status, HttpStatusCode.InternalServerError)
        assertEquals(result, "Please wait! I'm not ready :(")
    }
}

private fun ready(): ApplicationState {
    val state = ApplicationState()
    state.ready = true
    state.alive = true
    return state
}

private fun unready(): ApplicationState {
    val state = ApplicationState()
    state.ready = false
    state.alive = false
    return state
}
