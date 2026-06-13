package `in`.procyk.bring.service

import arrow.core.Either
import `in`.procyk.bring.RecipeIngredient
import `in`.procyk.bring.service.CookingRecipeService.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.job
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.uuid.Uuid

internal class CookingRecipeTest : BringServerIntegrationTestCase() {

    @Test
    fun `create and get cooking recipe`() = runTest {
        val service = cookingRecipeService()
        val name = "Pancakes"
        val ingredients = listOf(
            RecipeIngredient(name = "flour", measures = 200.0, unit = "g"),
            RecipeIngredient(name = "milk", measures = 300.0, unit = "ml"),
        )
        val steps = listOf("Mix all ingredients", "Fry on the pan")

        val recipeId = service.testCall {
            createCookingRecipe(name, ingredients, steps, userId)
        }

        val data = service.testCall {
            getCookingRecipe(recipeId)
        }

        assertEquals(recipeId, data.id)
        assertEquals(name, data.name)
        assertEquals(userId, data.byUserId)
        assertEquals(ingredients, data.ingredients)
        assertEquals(steps, data.steps)
    }

    @Test
    fun `create cooking recipe with blank name returns InvalidName`() = runTest {
        val service = cookingRecipeService()

        val result = coroutineScope {
            val deferred = CompletableDeferred<Either<Uuid, AddCookingRecipeError>>(parent = coroutineContext.job)
            service.durableCall {
                deferred.complete(createCookingRecipe("   ", emptyList(), emptyList(), userId))
            }
            deferred.await()
        }

        val error = assertIs<Either.Right<AddCookingRecipeError>>(result).value
        assertEquals(AddCookingRecipeError.InvalidName, error)
    }

    @Test
    fun `get unknown cooking recipe returns UnknownRecipeId`() = runTest {
        val service = cookingRecipeService()

        val result = coroutineScope {
            val deferred = CompletableDeferred<Either<Any, GetCookingRecipeError>>(parent = coroutineContext.job)
            service.durableCall {
                deferred.complete(getCookingRecipe(Uuid.random()))
            }
            deferred.await()
        }

        val error = assertIs<Either.Right<GetCookingRecipeError>>(result).value
        assertEquals(GetCookingRecipeError.UnknownRecipeId, error)
    }

    @Test
    fun `remove unknown cooking recipe returns UnknownRecipeId`() = runTest {
        val service = cookingRecipeService()

        val result = coroutineScope {
            val deferred = CompletableDeferred<Either<Unit, RemoveCookingRecipeError>>(parent = coroutineContext.job)
            service.durableCall {
                deferred.complete(removeCookingRecipe(Uuid.random(), userId))
            }
            deferred.await()
        }

        val error = assertIs<Either.Right<RemoveCookingRecipeError>>(result).value
        assertEquals(RemoveCookingRecipeError.UnknownRecipeId, error)
    }

    @Test
    fun `remove cooking recipe by owner deletes it`() = runTest {
        val service = cookingRecipeService()
        val recipeId = service.testCall {
            createCookingRecipe(
                "To be removed",
                listOf(RecipeIngredient(name = "salt", measures = 1.0, unit = "tsp")),
                listOf("Add salt"),
                userId,
            )
        }

        service.testCall {
            removeCookingRecipe(recipeId, userId)
        }

        val result = coroutineScope {
            val deferred = CompletableDeferred<Either<Any, GetCookingRecipeError>>(parent = coroutineContext.job)
            service.durableCall {
                deferred.complete(getCookingRecipe(recipeId))
            }
            deferred.await()
        }

        val error = assertIs<Either.Right<GetCookingRecipeError>>(result).value
        assertEquals(GetCookingRecipeError.UnknownRecipeId, error)
    }

    @Test
    fun `remove cooking recipe by non-owner does not delete it`() = runTest {
        val service = cookingRecipeService()
        val name = "Owner's recipe"
        val ingredients = listOf(RecipeIngredient(name = "sugar", measures = 50.0, unit = "g"))
        val steps = listOf("Add sugar")

        val recipeId = service.testCall {
            createCookingRecipe(name, ingredients, steps, userId)
        }

        val otherUserId = Uuid.random()
        service.testCall {
            removeCookingRecipe(recipeId, otherUserId)
        }

        val data = service.testCall {
            getCookingRecipe(recipeId)
        }

        assertEquals(recipeId, data.id)
        assertEquals(name, data.name)
        assertEquals(userId, data.byUserId)
        assertEquals(ingredients, data.ingredients)
        assertEquals(steps, data.steps)
    }
}
