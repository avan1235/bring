package `in`.procyk.savvry.test

import android.content.ContentProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.github.takahirom.roborazzi.captureRoboImage
import `in`.procyk.savvry.SavvryAppInternal
import `in`.procyk.savvry.SavvryStore
import `in`.procyk.savvry.R
import `in`.procyk.savvry.ui.SavvryAppTheme
import `in`.procyk.savvry.ui.components.Text
import `in`.procyk.savvry.vm.PlatformContext
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
import kotlin.uuid.Uuid


@RunWith(AndroidJUnit4::class)
@GraphicsMode(Mode.NATIVE)
@Config(
    qualifiers = "w411dp-h842dp-xxhdpi",
)
internal abstract class SavvryAppCreateScreenshotTest {

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

    protected inline fun screenshotSavvryApp(
        modifier: Modifier = Modifier,
        crossinline config: SavvryStore.() -> SavvryStore = { this },
        crossinline text: @Composable BoxScope.() -> Unit = {},
        crossinline context: ComposeContentTestRule.() -> Unit = {},
    ): Unit = with(composeTestRule) {
        setContent {
            SavvryAppTheme(PlatformContext(LocalContext.current)) { context ->
                LaunchedEffect(Unit) {
                    context.store.update {
                        it?.copy(useLiquidGlassNavigation = true)?.config()
                    }
                }
                Box(
                    modifier = Modifier
                        .background(SavvryAppTheme.colors.surface)
                        .fillMaxSize(),
                ) {
                    Box(
                        modifier = Modifier
                            .background(SavvryAppTheme.colors.primary.copy(alpha = 0.25f))
                            .fillMaxSize(),
                    ) {
                        text()
                    }
                }
                Box(
                    modifier = modifier.then(DefaultSizeModifier),
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 18.dp, start = 16.dp, end = 32.dp)
                            .clip(RoundedCornerShape(36.dp))
                            .background(SavvryAppTheme.colors.background)
                            .padding(top = 32.dp, bottom = 16.dp)
                            .fillMaxSize(),

                        ) {
                        SavvryAppInternal(context)
                    }
                }
                Popup {
                    Box(
                        modifier = modifier.then(DefaultSizeModifier),
                    ) {
                        Image(
                            modifier = Modifier
                                .zIndex(Float.MAX_VALUE),
                            painter = painterResource(R.drawable.pixel),
                            contentDescription = null,
                        )
                    }
                }
            }
        }
        waitForIdle()

        waitUntilExactlyOneTestTagExists("liquid-bottom-tabs")

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
        maxLines = Int.MAX_VALUE,
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

    waitUntilExactlyOneTextExists("Savvry")
}

internal fun ComposeTestRule.navigateFavoritesScreen() {
    onNodeWithTag("button-navigate-favourites")
        .assertExists()
        .assertHasClickAction()
        .performClick()

    waitUntilExactlyOneTestTagExists("screen-favorites")
}

internal fun ComposeTestRule.navigateLoyaltyCardsScreen() {
    onNodeWithTag("button-navigate-loyaltycards")
        .assertExists()
        .assertHasClickAction()
        .performClick()

    waitUntilExactlyOneTestTagExists("screen-loyalty-cards")
}

internal fun ComposeTestRule.navigateRecipesScreen() {
    onNodeWithTag("button-navigate-recipes")
        .assertExists()
        .assertHasClickAction()
        .performClick()

    waitUntilExactlyOneTestTagExists("screen-recipes")
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
    waitForExpanded: String? = null,
    clickWaitFor: Boolean = false,
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

    if (waitForExpanded != null) {
        waitUntilExactlyOneTestTagExists(waitForExpanded)
    }
    if (clickWaitFor && waitForExpanded != null) {
        onNodeWithTag(waitForExpanded)
            .assertExists()
            .assertHasClickAction()
            .performClick()
    }
}
