package `in`.procyk.savvry.ui.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import savvry.app.generated.resources.*
import `in`.procyk.savvry.runIf
import `in`.procyk.savvry.ui.SavvryAppTheme
import `in`.procyk.savvry.ui.components.*
import `in`.procyk.savvry.ui.components.liquid.LiquidBottomTabsSpacer
import `in`.procyk.savvry.ui.components.progressindicators.LinearProgressIndicator
import `in`.procyk.savvry.ui.contentColorFor
import `in`.procyk.savvry.vm.RecipeViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RecipeScreen(
    padding: PaddingValues,
    vm: RecipeViewModel,
    rounded: Dp = 12.dp,
    stepsBackground: Color = SavvryAppTheme.colors.primary,
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
                contentPadding = PaddingValues(bottom = 16.dp, top = 16.dp),
            ) {
                item("${recipe.id}-name") {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = recipe.name,
                            style = SavvryAppTheme.typography.h1,
                            maxLines = Int.MAX_VALUE,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            content = {
                                Icon(Icons.Outlined.DeleteOutline)
                            },
                            variant = IconButtonVariant.SecondaryGhost,
                            onClick = { vm.context.showSnackbar(Res.string.long_click_to_remove) },
                            onLongClick = vm::onRemoveRecipe,
                        )
                        IconButton(
                            content = {
                                Icon(Icons.Outlined.IosShare)
                            },
                            variant = IconButtonVariant.SecondaryGhost,
                            onClick = vm::onShareRecipe,
                        )
                    }
                }
                item("${recipe.id}-ingredients-title") {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(Res.string.ingredients),
                            style = SavvryAppTheme.typography.h2,
                            modifier = Modifier.weight(1f),
                        )
                        NumberRow(
                            minVisible = 2,
                            value = scale.toInt(),
                            onValueChange = { vm.setScale(it.toDouble()) },
                        )
                        IconButton(
                            content = {
                                Icon(Icons.Outlined.Summarize)
                            },
                            variant = IconButtonVariant.SecondaryGhost,
                            onClick = vm::onCreateShoppingListFromRecipe,
                        )
                    }
                }
                item("${recipe.id}-ingredients") {
                    Box(
                        modifier = Modifier
                            .border(1.dp, stepsBackground, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                            .fillMaxWidth(),
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                recipe.ingredients.forEachIndexed { idx, ingredient ->
                                    val description = "- ${ingredient.toString(scale)}"
                                    withStyle(
                                        style = ParagraphStyle(
                                            textIndent = TextIndent(firstLine = 0.sp, restLine = 13.sp),
                                            lineHeight = 24.sp,
                                        )
                                    ) {
                                        if (idx < recipe.ingredients.lastIndex) appendLine(description)
                                        else append(description)
                                    }
                                }
                            },
                            style = SavvryAppTheme.typography.body1,
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
                        text = stringResource(Res.string.steps),
                        style = SavvryAppTheme.typography.h2,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
                itemsIndexed(recipe.steps, key = { idx, _ -> "${recipe.id}-step-$idx" }) { index, step ->
                    val textColor = contentColorFor(stepsBackground)
                    val stepsBackground by remember { derivedStateOf { stepsBackground.copy(alpha = 0.6f) } }
                    val alpha by animateFloatAsState(
                        if (doneStep >= index) 0.1f else 1f,
                        animationSpec = tween(durationMillis = 500, easing = LinearEasing),
                    )
                    BoxWithConstraints {
                        val width = constraints.maxWidth
                        Text(
                            text = buildAnnotatedString {
                                val stepDescription = stringResource(Res.string.step_format, index + 1, step)
                                withStyle(
                                    style = ParagraphStyle(
                                        textIndent = TextIndent(firstLine = 0.sp, restLine = 18.sp),
                                    )
                                ) {
                                    append(stepDescription)
                                }
                            },
                            style = SavvryAppTheme.typography.body1,
                            modifier = Modifier
                                .alpha(alpha)
                                .runIf(index == 0 && recipe.steps.size > 1) {
                                    background(
                                        stepsBackground,
                                        RoundedCornerShape(topStart = rounded, topEnd = rounded),
                                    )
                                }
                                .runIf(index == recipe.steps.lastIndex && recipe.steps.size > 1) {
                                    background(
                                        stepsBackground,
                                        RoundedCornerShape(bottomStart = rounded, bottomEnd = rounded),
                                    )
                                }
                                .runIf(index in 1..<recipe.steps.lastIndex && recipe.steps.size > 1) {
                                    background(stepsBackground)
                                }
                                .runIf(recipe.steps.size == 1) {
                                    background(stepsBackground, RoundedCornerShape(rounded))
                                }
                                .padding(
                                    top = if (index == 0) 16.dp else 8.dp,
                                    bottom = if (index == recipe.steps.lastIndex) 16.dp else 8.dp,
                                    start = 8.dp,
                                    end = 8.dp,
                                )
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            if (it.x > width / 2) vm.onNextStep() else vm.onPrevStep()
                                        },
                                    )
                                }
                                .pointerInput(Unit) {
                                    var amount = 0f
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            val lastAmount = amount
                                            amount = 0f
                                            if (lastAmount > 0) vm.onNextStep() else if (lastAmount < 0) vm.onPrevStep()
                                        },
                                    ) { _, dragAmount ->
                                        amount += dragAmount
                                    }
                                }
                                .testTag("recipe-step-${if (doneStep >= index) "done" else "in-progress"}-$index")
                                .fillMaxWidth(),
                            maxLines = Int.MAX_VALUE,
                            color = textColor,
                        )
                    }
                }
                item("spacer") {
                    LiquidBottomTabsSpacer(vm)
                }
            }
        }
    }
}
