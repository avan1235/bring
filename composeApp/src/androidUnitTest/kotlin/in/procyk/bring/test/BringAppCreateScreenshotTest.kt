package `in`.procyk.bring.test

import android.content.ContentProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import bring.composeapp.generated.resources.Res
import bring.composeapp.generated.resources.pixel
import com.github.takahirom.roborazzi.captureRoboImage
import `in`.procyk.bring.BringAppInternal
import `in`.procyk.bring.BringStore
import `in`.procyk.bring.ui.BringAppTheme
import org.jetbrains.compose.resources.painterResource
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.rules.TestName
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.GraphicsMode.Mode
import java.lang.Class.forName
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.random.Random
import kotlin.uuid.Uuid


@RunWith(AndroidJUnit4::class)
@GraphicsMode(Mode.NATIVE)
@Config(
    qualifiers = "w411dp-h842dp-xxhdpi"
)
internal abstract class BringAppCreateScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    var name: TestName = TestName()

    @Before
    fun setup() {
        val clazz = forName("org.jetbrains.compose.resources.AndroidContextProvider")
        @Suppress("UNCHECKED_CAST")
        Robolectric.setupContentProvider(clazz as Class<ContentProvider>)

        System.setProperty("roborazzi.test.record", "true")
    }

    companion object Companion {
        protected const val EDIT_LIST_ID: String = "3ef9c65e-5961-40e0-a41e-fa8362a69c15"

        val DefaultSizeModifier: Modifier = Modifier.size(411.dp, 842.dp)
        const val MidScale: Float = 0.75f
        val MidScaleShift: Dp = 64.dp

        private val screenshotsDirectory: Path = Path("screenshots")

        @BeforeClass
        @JvmStatic
        fun setupAll() {
            Files.createDirectories(screenshotsDirectory)
        }
    }

    protected fun randomShoppingListId(): String =
        Uuid.random().toHexDashString()

    protected inline fun screenshotBringApp(
        initListId: String? = null,
        modifier: Modifier = Modifier,
        crossinline config: BringStore.() -> BringStore = { this },
        crossinline context: ComposeContentTestRule.() -> Unit = {},
    ): Unit = with(composeTestRule) {
        setContent {
            BringAppTheme { context ->
                LaunchedEffect(Unit) {
                    context.store.update { it?.copy(themeColor = Color(Random.nextLong()).toArgb())?.config() }
                }
                Box(
                    modifier = Modifier
                        .background(BringAppTheme.colors.surface)
                        .fillMaxSize()
                )
                Box(
                    modifier = modifier.then(DefaultSizeModifier)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 18.dp, start = 16.dp, end = 32.dp)
                            .clip(RoundedCornerShape(36.dp))
                            .background(BringAppTheme.colors.background)
                            .padding(top = 32.dp)
                            .fillMaxSize()

                    ) {
                        BringAppInternal(context, initListId)
                    }
                    Image(
                        painter = painterResource(Res.drawable.pixel),
                        contentDescription = null,
                    )
                }
            }
        }
        waitForIdle()

        context()

        onRoot().captureRoboImage("${screenshotsDirectory.resolve(name.methodName.replace(' ', '-'))}.png")
    }
}

internal const val WAIT_TIMEOUT_MILLIS: Long = 10_000L

@OptIn(ExperimentalTestApi::class)
internal fun ComposeTestRule.waitUntilAtLeastOneTestTagExists(tag: String) =
    waitUntilAtLeastOneExists(hasTestTag(tag), WAIT_TIMEOUT_MILLIS)

@OptIn(ExperimentalTestApi::class)
internal fun ComposeTestRule.waitUntilAtLeastOneTextExists(text: String) =
    waitUntilAtLeastOneExists(hasText(text), WAIT_TIMEOUT_MILLIS)