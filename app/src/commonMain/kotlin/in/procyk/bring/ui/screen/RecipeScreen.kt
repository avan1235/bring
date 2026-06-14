package `in`.procyk.bring.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import `in`.procyk.bring.runIf
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.ui.LocalContentColor
import `in`.procyk.bring.ui.components.*
import `in`.procyk.bring.ui.components.liquid.LiquidBottomTabsSpacer
import `in`.procyk.bring.ui.components.progressindicators.LinearProgressIndicator
import `in`.procyk.bring.vm.RecipeViewModel

@Composable
internal fun RecipeScreen(
    padding: PaddingValues,
    vm: RecipeViewModel,
) = AppScreen("screen-recipe", padding) {
    val recipeData by vm.recipe.collectAsState()
    val scale by vm.scale.collectAsState()

    when (val recipe = recipeData) {
        null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LinearProgressIndicator()
        }

        else -> {
            val doneStep by vm.doneStep.collectAsState()
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                item("${recipe.id}-name") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = recipe.name,
                        style = BringAppTheme.typography.h1,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                }
                item("${recipe.id}-scale") {
                    Row {
                        NumberRow(
                            value = scale.toInt(),
                            onValueChange = { vm.setScale(it.toDouble()) },
                        ) {
                            Text("Scale:")
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            content = {
                                Icon(Icons.Outlined.IosShare)
                            },
                            variant = IconButtonVariant.SecondaryGhost,
                            onClick = { vm.onShareRecipe() },
                        )
                    }
                }
                item("${recipe.id}-ingredients-title") {
                    Text(
                        text = "Ingredients",
                        style = BringAppTheme.typography.h2,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
                item("${recipe.id}-ingredients") {
                    Column(
                        Modifier
                            .border(1.dp, BringAppTheme.colors.primary, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .fillMaxWidth(),
                    ) {

                        Text(
                            text = buildAnnotatedString {
                                withBulletList {
                                    recipe.ingredients.forEach { ingredient ->
                                        val scaledMeasure = ingredient.measures * scale
                                        withBulletListItem {
                                            append(
                                                "${
                                                    scaledMeasure.toString().removeSuffix(".0")
                                                } ${ingredient.unit} ${ingredient.name}",
                                            )
                                        }
                                    }
                                }
                            },
                            style = BringAppTheme.typography.body1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            maxLines = Int.MAX_VALUE,
                        )
                    }
                }
                item("${recipe.id}-steps-title") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Steps",
                        style = BringAppTheme.typography.h2,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
                itemsIndexed(recipe.steps, key = { idx, _ -> "${recipe.id}-step-$idx" }) { index, step ->
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = BringAppTheme.colors.onPrimary,
                                    background = BringAppTheme.colors.primary,
                                ),
                            ) {
                                append(" ${index + 1}. ")
                            }
                            append(" $step")
                        },
                        style = BringAppTheme.typography.body1,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .runIf(doneStep >= index) {
                                background(
                                    BringAppTheme.colors.disabled,
                                    RoundedCornerShape(4.dp),
                                )
                            }
                            .border(1.dp, BringAppTheme.colors.secondary, RoundedCornerShape(4.dp))
                            .padding(4.dp)
                            .fillMaxWidth(),
                        maxLines = Int.MAX_VALUE,
                        color = if (doneStep >= index) BringAppTheme.colors.onDisabled else LocalContentColor.current,
                    )
                }
                item("spacer") {
                    LiquidBottomTabsSpacer(vm)
                }
            }
        }
    }
}
