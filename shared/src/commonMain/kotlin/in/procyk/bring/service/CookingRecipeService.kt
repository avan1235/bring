package `in`.procyk.bring.service

import arrow.core.Either
import `in`.procyk.bring.CookingRecipeData
import `in`.procyk.bring.RecipeIngredient
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Rpc
interface CookingRecipeService {

    @Serializable
    enum class AddCookingRecipeError { InvalidName, InvalidIngredients, InvalidSteps, Internal }

    suspend fun createCookingRecipe(
        name: String,
        ingredients: List<RecipeIngredient>,
        steps: List<String>,
        byUserId: Uuid,
    ): Either<Uuid, AddCookingRecipeError>

    @Serializable
    enum class GetCookingRecipeError { UnknownRecipeId, Internal }

    suspend fun getCookingRecipe(
        recipeId: Uuid,
    ): Either<CookingRecipeData, GetCookingRecipeError>

    @Serializable
    enum class RemoveCookingRecipeError { UnknownRecipeId, Internal }

    suspend fun removeCookingRecipe(
        recipeId: Uuid,
        byUserId: Uuid,
    ): Either<Unit, RemoveCookingRecipeError>
}
