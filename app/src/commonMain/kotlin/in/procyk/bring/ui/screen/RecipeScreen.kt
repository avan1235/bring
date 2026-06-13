package `in`.procyk.bring.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.ui.components.AppScreen
import `in`.procyk.bring.ui.components.Icon
import `in`.procyk.bring.ui.components.IconButton
import `in`.procyk.bring.ui.components.Text
import `in`.procyk.bring.vm.RecipeViewModel
import `in`.procyk.bring.ui.components.progressindicators.LinearProgressIndicator

@Composable
internal fun RecipeScreen(
    padding: PaddingValues,
    vm: RecipeViewModel,
) = AppScreen("screen-recipe", padding) {
    val recipeData by vm.recipe.collectAsState()
    val scale by vm.scale.collectAsState()

    if (recipeData == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LinearProgressIndicator()
        }
    } else {
        val recipe = recipeData!!
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = recipe.name,
                    style = BringAppTheme.typography.h1,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Scale: ", style = BringAppTheme.typography.h3)
                    IconButton(onClick = { vm.setScale(scale - 0.5) }) {
                        Icon(Icons.Filled.Remove)
                    }
                    Text(
                        text = scale.toString(),
                        style = BringAppTheme.typography.h3,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    IconButton(onClick = { vm.setScale(scale + 0.5) }) {
                        Icon(Icons.Filled.Add)
                    }
                }
            }
            item {
                Text(
                    text = "Ingredients",
                    style = BringAppTheme.typography.h2,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            itemsIndexed(recipe.ingredients) { _, ingredient ->
                val scaledMeasure = ingredient.measures * scale
                Text(
                    text = "• ${scaledMeasure.toString().removeSuffix(".0")} ${ingredient.unit} ${ingredient.name}",
                    style = BringAppTheme.typography.body1,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Steps",
                    style = BringAppTheme.typography.h2,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            itemsIndexed(recipe.steps) { index, step ->
                Text(
                    text = "${index + 1}. $step",
                    style = BringAppTheme.typography.body1,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
