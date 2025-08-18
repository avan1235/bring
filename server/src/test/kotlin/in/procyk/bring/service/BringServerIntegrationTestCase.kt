package `in`.procyk.bring.service

import `in`.procyk.bring.*
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.ComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.File
import kotlin.uuid.Uuid

internal abstract class BringServerIntegrationTestCase {

    private val dotenv = dotenv {
        ignoreIfMissing = true
        directory = "../"
    }

    private val SERVER_SERVICE_NAME: String = "bring-dev-server"
    private val SERVER_SERVICE_PORT: Int get() = dotenv.env("PORT")

    protected val composeContainer = ComposeContainer(File("../docker-compose.yml"))
        .withExposedService(
            SERVER_SERVICE_NAME,
            SERVER_SERVICE_PORT,
            Wait.forLogMessage(".*io\\.ktor\\.server\\.Application - Responding at.*", 1),
        )
        .withBuild(true)
        .withLocalCompose(true)

    protected val containerHost: String
        get() = composeContainer.getServiceHost(SERVER_SERVICE_NAME, SERVER_SERVICE_PORT)

    protected val containerPort: Int
        get() = composeContainer.getServicePort(SERVER_SERVICE_NAME, SERVER_SERVICE_PORT)

    protected val httpClient = HttpClient {
        defaultRequest {
            url(scheme = "http", host = containerHost, port = containerPort)
        }
    }

    protected fun TestScope.shoppingListService() =
        DurableRpcService<ShoppingListService>(backgroundScope, httpClient) {
            url(scheme = "ws", host = containerHost, port = containerPort, path = ShoppingListRpcPath)
        }

    protected fun TestScope.favoriteElementService() =
        DurableRpcService<FavoriteElementService>(backgroundScope, httpClient) {
            url(scheme = "ws", host = containerHost, port = containerPort, path = FavoriteElementRpcPath)
        }

    protected val userId = Uuid.Companion.random()

    @BeforeEach
    fun setUpContainer() {
        composeContainer.start()
    }

    @AfterEach
    fun tearDownContainer() {
        composeContainer.stop()
    }
}

