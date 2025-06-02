package `in`.procyk.bring.test

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
internal class EditListScreenScreenshotTest : BringAppCreateScreenshotTest() {

    @Test
    fun `edit list screen options`() = screenshotBringApp(
        modifier = Modifier
            .absoluteOffset(y = 320.dp),
        config = {
            copy(
                useGemini = true,
                geminiKey = "fake-key",
                themeColor = Color.Green.toArgb(),
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(36.dp)
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BigScreenshotText(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Share")
                        }
                        append(" and ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Save")
                        }
                        appendLine(" Your")
                        append("lists as ")
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append("Favorite")
                        }
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
                BigScreenshotText(
                    text = AnnotatedString("&"),
                )
                Spacer(modifier = Modifier.height(8.dp))
                BigScreenshotText(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Build")
                        }
                        appendLine(" your lists")
                        append("with the help of ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                            append("AI")
                        }
                    },
                )
            }
        }
    ) {
        createShoppingList(
            listName = "Clean Start",
            clickButton = "button-toggle-gemini",
        )

        onNodeWithTag("button-expand-options")
            .assertExists()
            .assertHasClickAction()
            .performClick()

        listOf("Eggs", "Chicken", "Avocados", "Baby spinach", "Blueberries", "Bananas", "Greek yogurt").withIndex()
            .forEach { (idx, text) ->
                onNodeWithTag("text-field-add-list-item")
                    .assertExists()
                    .performTextInput(text)

                onNodeWithTag("button-add-list-item")
                    .assertExists()
                    .performClick()

                waitUntilNodeCount("list-item", idx + 1)

                if (idx in setOf(0, 2, 3, 5)) {
                    onAllNodesWithTag("checkbox-list-item")
                        .onFirst()
                        .assertExists()
                        .assertHasClickAction()
                        .performClick()
                }
            }

        onNodeWithTag("button-expand-options")
            .assertExists()
            .assertHasClickAction()
            .performClick()

        waitUntilExactlyOneTestTagExists("button-toggle-gemini")

        onNodeWithTag("button-toggle-gemini")
            .assertExists()
            .assertHasClickAction()
            .performClick()

        waitUntilExactlyOneTestTagExists("button-toggle-favorite")
    }
}