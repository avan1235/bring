package `in`.procyk.savvry.test

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import `in`.procyk.savvry.CookingRecipe
import `in`.procyk.savvry.CookingRecipeData
import `in`.procyk.savvry.RecipeIngredient
import org.junit.Test
import kotlin.time.Instant
import kotlin.uuid.Uuid

@OptIn(ExperimentalTestApi::class)
internal class RecipeScreenScreenshotTest : SavvryAppCreateScreenshotTest() {

    @Test
    fun `recipes screen`() = screenshotSavvryApp(
        modifier = Modifier
            .absoluteOffset(y = MidScaleShift)
            .scale(MidScale),
        config = {
            copy(
                themeColor = Color.Yellow.toArgb(),
                recipes = sampleRecipes(userId),
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(vertical = 36.dp, horizontal = 18.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BigScreenshotText(
                    text = buildAnnotatedString {
                        append("Keep ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            appendLine("All")
                        }
                        append(" Your Recipes")
                    },
                )
            }
        },
    ) {
        navigateRecipesScreen()

        waitUntilNodeCount("recipe", 2)
    }

    @Test
    fun `recipe screen`() = screenshotSavvryApp(
        modifier = Modifier
            .scale(MidScale),
        config = {
            copy(
                themeColor = Color.Red.toArgb(),
                recipes = sampleRecipes(userId),
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(vertical = 36.dp, horizontal = 18.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BigScreenshotText(
                    text = buildAnnotatedString {
                        append("Cook ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Step")
                        }
                        appendLine(" by Step")
                    },
                )
                BigScreenshotText(
                    text = buildAnnotatedString {
                        append("and ")
                        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                            append("scale")
                        }
                        append(" ingredients")
                    },
                )
            }
        },
    ) {
        navigateRecipesScreen()

        waitUntilNodeCount("recipe", 2)

        onAllNodesWithTag("recipe")
            .onFirst()
            .assertHasClickAction()
            .performClick()

        waitUntilExactlyOneTestTagExists("screen-recipe")

        waitUntilExactlyOneTestTagExists("recipe-step-in-progress-0")
        onAllNodesWithTag("recipe-step-in-progress-0")
            .onFirst()
            .performTouchInput { doubleClick(centerRight) }

        waitUntilExactlyOneTestTagExists("recipe-step-done-0")
    }
}

private fun sampleRecipes(userId: Uuid): List<CookingRecipe> {
    val createdAt = Instant.fromEpochSeconds(1_700_000_000)
    return listOf(
        "76798ee6-6385-4bc6-964a-e6a8a8938981" to CookingRecipeData(
            id = Uuid.parse("76798ee6-6385-4bc6-964a-e6a8a8938981"),
            name = "Scrambled Eggs",
            byUserId = userId,
            createdAt = createdAt,
            ingredients = listOf(
                RecipeIngredient(name = "eggs", measures = 3.0, unit = "pcs"),
                RecipeIngredient(name = "milk", measures = 50.0, unit = "ml"),
                RecipeIngredient(name = "butter", measures = 10.0, unit = "g"),
                RecipeIngredient(name = "salt", measures = 1.0, unit = "pinch"),
            ),
            steps = listOf(
                "Whisk the eggs and milk in a bowl with a pinch of salt.",
                "Melt the butter in a pan over medium heat.",
                "Pour in the egg mixture and cook until set, stirring occasionally.",
            ),
        ),
        "6022262e-b31a-450c-a83b-6c9f93bfa529" to CookingRecipeData(
            id = Uuid.parse("6022262e-b31a-450c-a83b-6c9f93bfa529"),
            name = "Spaghetti Pomodoro",
            byUserId = userId,
            createdAt = createdAt,
            ingredients = listOf(
                RecipeIngredient(name = "spaghetti", measures = 200.0, unit = "g"),
                RecipeIngredient(name = "tomato sauce", measures = 400.0, unit = "ml"),
                RecipeIngredient(name = "onion", measures = 1.0, unit = "pc"),
                RecipeIngredient(name = "olive oil", measures = 2.0, unit = "tbsp"),
            ),
            steps = listOf(
                "Boil the water and cook spaghetti according to package instructions.",
                "Chop the onion and saute in olive oil until translucent.",
                "Add tomato sauce to the onion and simmer for 10 minutes.",
                "Drain pasta and mix with the sauce.",
            ),
        ),
    ).mapIndexed { idx, (uuid, data) ->
        CookingRecipe(
            recipeId = Uuid.parse(uuid),
            order = idx.toDouble() + 1.0,
            cachedData = data,
        )
    }
}
