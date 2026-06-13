package `in`.procyk.bring.service

import `in`.procyk.bring.*
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.DockerClientFactory
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

    init {
        ensureServerImageBuilt()
    }

    protected val composeContainer = ComposeContainer(
        File("../docker-compose.yml"),
        File("../docker-compose.test.yml"),
    )
        .withExposedService(
            SERVER_SERVICE_NAME,
            SERVER_SERVICE_PORT,
            Wait.forLogMessage(".*io\\.ktor\\.server\\.Application - Responding at.*", 1),
        )
        .withBuild(false)

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

    protected fun TestScope.loyaltyCardService() =
        DurableRpcService<LoyaltyCardService>(backgroundScope, httpClient) {
            url(scheme = "ws", host = containerHost, port = containerPort, path = LoyaltyCardRpcPath)
        }

    protected fun TestScope.cookingRecipeService() =
        DurableRpcService<CookingRecipeService>(backgroundScope, httpClient) {
            url(scheme = "ws", host = containerHost, port = containerPort, path = CookingRecipeRpcPath)
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

    companion object {
        private const val SERVER_IMAGE_TAG = "bring-dev-server:test"

        @Volatile
        private var imageEnsured = false

        @Synchronized
        fun ensureServerImageBuilt() {
            if (imageEnsured) return
            val forceRebuild = System.getenv("REBUILD_SERVER_IMAGE") == "true"
            val client = DockerClientFactory.lazyClient()
            val exists = client.listImagesCmd()
                .withImageNameFilter(SERVER_IMAGE_TAG)
                .exec()
                .any { img -> img.repoTags?.any { it == SERVER_IMAGE_TAG } == true }
            if (!exists || forceRebuild) {
                buildServerImageWithBuildKit()
            }
            imageEnsured = true
        }

        private fun buildServerImageWithBuildKit() {
            // docker-java's ImageFromDockerfile uses the legacy build API which does
            // not support BuildKit `--mount=type=cache` directives.
            val projectRoot = File("..").canonicalFile
            val pb = ProcessBuilder(
                "docker", "build",
                "-f", "Dockerfile",
                "-t", SERVER_IMAGE_TAG,
                ".",
            )
                .directory(projectRoot)
                .inheritIO()
            pb.environment()["DOCKER_BUILDKIT"] = "1"
            val process = pb.start()
            val exit = process.waitFor()
            if (exit != 0) {
                throw IllegalStateException("docker build for $SERVER_IMAGE_TAG failed with exit code $exit")
            }
        }
    }
}

