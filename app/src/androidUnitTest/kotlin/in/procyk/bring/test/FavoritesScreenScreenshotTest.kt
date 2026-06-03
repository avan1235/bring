package `in`.procyk.bring.test

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
internal class FavoritesScreenScreenshotTest : BringAppCreateScreenshotTest() {

    @Test
    fun `favorites screen collections`() = screenshotBringApp(
        modifier = Modifier
            .absoluteOffset(y = 160.dp),
        config = {
            copy(themeColor = Color.Yellow.toArgb())
        },
        text = {
            Box(
                modifier = Modifier
                    .padding(36.dp)
                    .align(Alignment.TopCenter),
            ) {
                BigScreenshotText(
                    text = buildAnnotatedString {
                        append("Save ")
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            appendLine("Favorites")
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Items")
                        }
                        append(" and ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Lists")
                        }
                    },
                )
            }
        }
    ) {
        createShoppingList(
            listName = "Cleaning Supplies",
            clickButton = "button-toggle-favorite",
        )
        navigateCleanMainScreen()
        createShoppingList(
            listName = "Dinner Ingredients",
            clickButton = "button-toggle-favorite",
        )
        navigateCleanMainScreen()
        createShoppingList(
            listName = "Weekend Shopping",
            clickButton = "button-toggle-favorite",
        )

        navigateFavoritesScreen()

        listOf("Milk", "Bread", "Eggs", "Fruits", "Vegetables", "Meat").withIndex().forEach { (idx, text) ->
            onNodeWithTag("text-field-add-favourite-element")
                .assertExists()
                .performTextInput(text)

            onNodeWithTag("button-add-favourite-element")
                .assertExists()
                .performClick()

            waitUntilNodeCount("chip-item", idx + 1)
        }
    }
}