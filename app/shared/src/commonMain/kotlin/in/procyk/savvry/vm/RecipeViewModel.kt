package `in`.procyk.savvry.vm

import androidx.lifecycle.viewModelScope
import savvry.app.generated.resources.*
import `in`.procyk.savvry.CookingRecipeData
import `in`.procyk.savvry.CookingRecipeRpcPath
import `in`.procyk.savvry.ShoppingListRpcPath
import `in`.procyk.savvry.service.CookingRecipeService
import `in`.procyk.savvry.service.ShoppingListService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class RecipeViewModel(
    context: Context,
    val recipeId: String,
) : AbstractViewModel(context) {

    private val parsedRecipeId = Uuid.parse(recipeId)

    private val cookingRecipeService = durableRpcService<CookingRecipeService>(CookingRecipeRpcPath)

    private val shoppingListService = durableRpcService<ShoppingListService>(ShoppingListRpcPath)

    private val _recipe = MutableStateFlow<CookingRecipeData?>(null)
    val recipe = _recipe.asStateFlow()

    private val _scale = MutableStateFlow(1.0)
    val scale = _scale.asStateFlow()

    private val _doneStep = MutableStateFlow(-1)
    val doneStep = _doneStep.asStateFlow()

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

    fun onCreateShoppingListFromRecipe() {
        val userId = store.userId
        val scale = scale.value
        viewModelScope.launch {
            shoppingListService.durableCall {
                createNewShoppingListFromRecipe(userId, parsedRecipeId, scale).fold(
                    ifLeft = { context.navigateEditList(it, fetchSuggestions = false) },
                    ifRight = { context.showSnackbar(Res.string.error_creating_list) }
                )
            }
        }
    }

    fun onShareRecipe() {
        viewModelScope.launch {
            onShareRecipe(recipeId, context)
        }
    }

    fun onRemoveRecipe() {
        val byUserId = store.userId
        viewModelScope.launch {
            updateConfig { it.copy(recipes = it.recipes.filterNot { it.recipeId == parsedRecipeId }) }

            cookingRecipeService.durableCall {
                removeCookingRecipe(parsedRecipeId, byUserId).fold(
                    ifLeft = {
                        context.showSnackbar(Res.string.recipe_removed)
                        context.navigateRecipes()
                    },
                    ifRight = {
                        context.showSnackbar(Res.string.error_removing_recipe)
                        context.navigateRecipes()
                    }
                )
            }
        }
    }

    fun onNextStep() {
        _doneStep.update { if (it < (recipe.value?.steps?.lastIndex ?: Int.MIN_VALUE)) it + 1 else it }
    }

    fun onPrevStep() {
        _doneStep.update { if (it > -1) it - 1 else it }
    }
}
