package `in`.procyk.bring.test

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
internal class CreateListScreenScreenshotTest : BringAppCreateScreenshotTest() {

    @Test
    fun `create list screen custom name`() = screenshotBringApp(
        modifier = Modifier
            .absoluteOffset(y = MidScaleShift)
            .scale(MidScale),
        config = {
            copy(themeColor = Color.Blue.toArgb())
        },
        text = {
            Box(
                modifier = Modifier
                    .padding(36.dp)
                    .align(Alignment.TopCenter)
            ) {
                BigScreenshotText(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Create")
                        }
                        append(" ")
                        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                            appendLine("custom")
                        }
                        append("shopping lists")
                    },
                )
            }
        }
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
            .performTextInput("Morning Shopping")

        waitUntilExactlyOneTextExists("Morning Shopping")
    }

    @Test
    fun `create list screen join list`() = screenshotBringApp(
        modifier = Modifier
            .absoluteOffset(y = -MidScaleShift)
            .scale(MidScale),
        config = {
            copy(themeColor = Color.Red.toArgb())
        },
        text = {
            Box(
                modifier = Modifier
                    .padding(36.dp)
                    .align(Alignment.BottomCenter)
            ) {
                BigScreenshotText(
                    text = buildAnnotatedString {
                        append("… or ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            appendLine("Join")
                        }
                        append("existing ones")
                    },
                )
            }
        }
    ) {
        val listId = randomShoppingListId()

        onNodeWithTag("screen-create-list")
            .assertExists()

        onNodeWithTag("text-field-join-list-id")
            .assertExists()
            .assertHasClickAction()
            .performClick()
            .performTextInput(listId)

        waitUntilExactlyOneTextExists(listId)
    }
}