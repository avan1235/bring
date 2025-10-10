package `in`.procyk.bring.service

import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class BringServerHealthcheckIntegrationTest : BringServerIntegrationTestCase() {

    @Test
    fun `bring-dev-server connection`() {
        assertEquals("localhost", containerHost)
        assertNotEquals(0, containerPort)
    }

    @Test
    fun `healthcheck with OK status`() = runTest {
        val status = httpClient.get("/health").status

        assertEquals(HttpStatusCode.Companion.OK, status)
    }
}
