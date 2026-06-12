package `in`.procyk.bring.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import `in`.procyk.bring.CookingRecipeData
import `in`.procyk.bring.RecipeIngredient
import `in`.procyk.bring.db.CookingRecipeEntity
import `in`.procyk.bring.db.CookingRecipesTable
import `in`.procyk.bring.service.CookingRecipeService.*
import kotlinx.datetime.toDeprecatedInstant
import kotlinx.datetime.toStdlibInstant
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

internal class CookingRecipeServiceImpl : CookingRecipeService {

    override suspend fun createCookingRecipe(
        name: String,
        ingredients: List<RecipeIngredient>,
        steps: List<String>,
        byUserId: Uuid,
    ): Either<Uuid, AddCookingRecipeError> {
        if (name.isBlank()) {
            return AddCookingRecipeError.InvalidName.right()
        }
        if (ingredients.any { it.name.isBlank() || it.unit.isBlank() }) {
            return AddCookingRecipeError.InvalidIngredients.right()
        }
        if (steps.any { it.isBlank() }) {
            return AddCookingRecipeError.InvalidSteps.right()
        }
        return txn(AddCookingRecipeError.Internal) {
            CookingRecipeEntity.new {
                this.name = name
                this.byUserId = byUserId.toJavaUuid()
                this.createdAt = Clock.System.now().toDeprecatedInstant()
                this.steps = steps
                this.ingredients = ingredients
            }.id.value.toKotlinUuid().left()
        }
    }

    override suspend fun getCookingRecipe(
        recipeId: Uuid,
    ): Either<CookingRecipeData, GetCookingRecipeError> {
        return txn(GetCookingRecipeError.Internal) {
            val recipe = CookingRecipeEntity.findById(recipeId.toJavaUuid())
            when (recipe) {
                null -> GetCookingRecipeError.UnknownRecipeId.right()
                else -> recipe.toCookingRecipeData().left()
            }
        }
    }

    override suspend fun removeCookingRecipe(
        recipeId: Uuid,
        byUserId: Uuid,
    ): Either<Unit, RemoveCookingRecipeError> {
        val recipeUUID = recipeId.toJavaUuid()
        return txn(RemoveCookingRecipeError.Internal) {
            val recipe = CookingRecipeEntity.findById(recipeUUID)
            when {
                recipe == null -> RemoveCookingRecipeError.UnknownRecipeId.right()
                recipe.byUserId != byUserId.toJavaUuid() -> Unit.left()
                else -> recipe.delete().left()
            }
        }
    }
}

private fun CookingRecipeEntity.toCookingRecipeData(): CookingRecipeData =
    CookingRecipeData(
        id = id.value.toKotlinUuid(),
        name = name,
        byUserId = byUserId.toKotlinUuid(),
        createdAt = createdAt.toStdlibInstant(),
        ingredients = ingredients,
        steps = steps,
    )
