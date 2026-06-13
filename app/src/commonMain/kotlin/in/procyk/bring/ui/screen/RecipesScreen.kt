package `in`.procyk.bring.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.twotone.Label
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import bring.app.generated.resources.Res
import bring.app.generated.resources.add_recipe
import bring.app.generated.resources.import_recipe
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.ui.LocalContentColor
import `in`.procyk.bring.ui.components.*
import `in`.procyk.bring.vm.RecipesViewModel
import `in`.procyk.bring.vm.RecipesViewModel.Recipe

@Composable
internal fun RecipesScreen(
    padding: PaddingValues,
    vm: RecipesViewModel,
) {
    ImportableCollectionScreen(
        testTag = "screen-recipes",
        padding = padding,
        vm = vm,
        addDialogTitle = Res.string.add_recipe,
        importDialogTitle = Res.string.import_recipe,
        scanButtonTestTag = "button-scan-recipe",
        itemContent = { recipe -> RecipeRow(recipe, vm) },
    )
}

@Composable
private fun RecipeRow(
    recipe: Recipe,
    vm: RecipesViewModel,
) {
    val enableEditMode by vm.enableEditMode.collectAsState()
    val selectedColor by vm.itemColor(recipe).collectAsState()
    val showLabels by vm.showLabels.collectAsState()
    val previousColor = remember { mutableStateOf<Color?>(null) }
    Row(
        modifier = Modifier
            .clickable { vm.context.navigateRecipe(recipe.id.toString()) }
            .padding(8.dp)
            .fillMaxWidth()
            .testTag("recipe"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            !showLabels -> {}
            enableEditMode -> IconButton(
                variant = IconButtonVariant.Ghost,
                onClick = { previousColor.value = selectedColor },
            ) {
                Icon(
                    imageVector = if (selectedColor != Color.Unspecified) Icons.AutoMirrored.TwoTone.Label else Icons.AutoMirrored.Outlined.Label,
                    tint = if (selectedColor != Color.Unspecified) selectedColor else LocalContentColor.current,
                )
            }

            else -> Icon(
                imageVector = if (selectedColor != Color.Unspecified) Icons.AutoMirrored.TwoTone.Label else Icons.AutoMirrored.Outlined.Label,
                tint = if (selectedColor != Color.Unspecified) selectedColor else LocalContentColor.current,
                modifier = Modifier.padding(8.dp),
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = recipe.data.name,
            maxLines = 1,
            style = BringAppTheme.typography.h3,
            modifier = Modifier.weight(1f, fill = true),
        )
        IconButton(
            variant = IconButtonVariant.Ghost,
            onClick = { vm.shareItem(recipe) },
        ) {
            Icon(Icons.Outlined.IosShare)
        }
        SelectColorDialog(
            selectedColor = selectedColor,
            previousColor = previousColor,
            onColorSaved = { vm.onItemColorUpdated(recipe.data.id, it) },
            onColorReset = { vm.onItemColorUpdated(recipe.data.id, null) },
        )
    }
}
