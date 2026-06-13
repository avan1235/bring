package `in`.procyk.bring.vm

import androidx.lifecycle.viewModelScope
import `in`.procyk.bring.CookingRecipeData
import `in`.procyk.bring.CookingRecipeRpcPath
import `in`.procyk.bring.service.CookingRecipeService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class RecipeViewModel(
    context: Context,
    val recipeId: String,
) : AbstractViewModel(context) {

    private val parsedRecipeId = Uuid.parse(recipeId)

    private val cookingRecipeService = durableRpcService<CookingRecipeService>(CookingRecipeRpcPath)

    private val _recipe = MutableStateFlow<CookingRecipeData?>(null)
    val recipe = _recipe.asStateFlow()

    private val _scale = MutableStateFlow(1.0)
    val scale = _scale.asStateFlow()

    init {
        val storedRecipe = store.recipes.find { it.recipeId == parsedRecipeId }
        val cached = storedRecipe?.cachedData
        if (cached != null) {
            _recipe.value = cached
        } else {
            viewModelScope.launch {
                val result = cookingRecipeService.durableCall { getCookingRecipe(parsedRecipeId) }
                result.onLeft { data ->
                    _recipe.value = data
                    updateConfig {
                        val newRecipes = it.recipes.map { r ->
                            if (r.recipeId == parsedRecipeId) r.copy(cachedData = data) else r
                        }
                        it.copy(recipes = newRecipes)
                    }
                }
            }
        }
    }

    fun setScale(newScale: Double) {
        if (newScale > 0) {
            _scale.value = newScale
        }
    }
}
