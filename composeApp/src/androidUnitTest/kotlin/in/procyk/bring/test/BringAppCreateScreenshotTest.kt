package `in`.procyk.bring.test

import android.content.ContentProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import androidx.test.ext.junit.runners.AndroidJUnit4
import bring.composeapp.generated.resources.Res
import bring.composeapp.generated.resources.pixel
import com.github.takahirom.roborazzi.captureRoboImage
import `in`.procyk.bring.BringAppInternal
import `in`.procyk.bring.BringStore
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.ui.components.Text
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
        modifier: Modifier = Modifier,
        crossinline config: BringStore.() -> BringStore = { this },
        crossinline text: @Composable BoxScope.() -> Unit = {},
        crossinline context: ComposeContentTestRule.() -> Unit = {},
    ): Unit = with(composeTestRule) {
        setContent {
            BringAppTheme { context ->
                LaunchedEffect(Unit) {
                    context.store.update {
                        it?.config()
                    }
                }
                Box(
                    modifier = Modifier
                        .background(BringAppTheme.colors.surface)
                        .fillMaxSize()
                ) {
                    text()
                }
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
                        BringAppInternal(context)
                    }
                }
                Popup {
                    Box(
                        modifier = modifier.then(DefaultSizeModifier)
                    ) {
                        Image(
                            modifier = Modifier
                                .zIndex(Float.MAX_VALUE),
                            painter = painterResource(Res.drawable.pixel),
                            contentDescription = null,
                        )
                    }
                }
            }
        }
        waitForIdle()

        context()

        onRoot().captureRoboImage("${screenshotsDirectory.resolve(name.methodName.replace(' ', '-'))}.png")
    }
}

@Composable
internal fun BigScreenshotText(text: AnnotatedString) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        fontSize = 36.sp,
        lineHeight = 52.sp,
        maxLines = Int.MAX_VALUE
    )
}

internal const val WAIT_TIMEOUT_MILLIS: Long = 60_000L

@OptIn(ExperimentalTestApi::class)
internal fun ComposeTestRule.waitUntilExactlyOneTestTagExists(tag: String) =
    waitUntilExactlyOneExists(hasTestTag(tag), WAIT_TIMEOUT_MILLIS)

@OptIn(ExperimentalTestApi::class)
internal fun ComposeTestRule.waitUntilNodeCount(tag: String, count: Int) =
    waitUntilNodeCount(hasTestTag(tag), count, WAIT_TIMEOUT_MILLIS)

@OptIn(ExperimentalTestApi::class)
internal fun ComposeTestRule.waitUntilExactlyOneTextExists(text: String) =
    waitUntilExactlyOneExists(hasText(text), WAIT_TIMEOUT_MILLIS)

internal fun ComposeTestRule.navigateCleanMainScreen() {
    onNodeWithTag("button-navigate-main")
        .assertExists()
        .assertHasClickAction()
        .performClick()

    waitUntilExactlyOneTestTagExists("screen-create-list")

    onNodeWithTag("button-navigate-main")
        .assertExists()
        .assertHasClickAction()
        .performClick()

    waitUntilExactlyOneTestTagExists("screen-create-list")

    waitUntilExactlyOneTextExists("Bring!")
}

internal fun ComposeTestRule.navigateFavoritesScreen() {
    onNodeWithTag("button-navigate-favourites")
        .assertExists()
        .assertHasClickAction()
        .performClick()

    waitUntilExactlyOneTestTagExists("screen-favorites")
}

internal fun ComposeTestRule.navigateSettingsScreen() {
    onNodeWithTag("button-navigate-settings")
        .assertExists()
        .assertHasClickAction()
        .performClick()

    waitUntilExactlyOneTestTagExists("screen-settings")
}

internal fun ComposeTestRule.createShoppingList(
    listName: String,
    clickButton: String
) {
    onNodeWithTag("screen-create-list")
        .assertExists()

    onNodeWithTag("text-field-create-list")
        .assertExists()
        .assertHasClickAction()
        .performClick()
        .performTextClearance()

    onNodeWithTag("text-field-create-list")
        .assertExists()
        .assertHasClickAction()
        .performClick()
        .performTextInput(listName)

    waitUntilExactlyOneTextExists(listName)

    onNodeWithTag("button-create-list")
        .assertExists()
        .assertHasClickAction()
        .performClick()

    waitUntilExactlyOneTestTagExists("screen-edit-list")
    waitUntilExactlyOneTextExists(listName)

    onNodeWithTag("button-expand-options")
        .assertExists()
        .assertHasClickAction()
        .performClick()

    waitUntilExactlyOneTestTagExists(clickButton)

    onNodeWithTag(clickButton)
        .assertExists()
        .assertHasClickAction()
        .performClick()
}
