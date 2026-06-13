package `in`.procyk.bring.vm

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.http.client.ktor.KtorKoogHttpClient
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.model.executeStructured
import ai.koog.prompt.message.AttachmentContent
import ai.koog.prompt.message.AttachmentSource
import ai.koog.prompt.structure.StructuredRequest.Manual
import ai.koog.prompt.structure.StructuredRequestConfig
import androidx.lifecycle.viewModelScope
import `in`.procyk.bring.CookingRecipeRpcPath
import `in`.procyk.bring.RecipeIngredient
import `in`.procyk.bring.ai.MarkdownWrappedJsonStructuredData.Companion.createMarkdownWrappedJsonStructure
import `in`.procyk.bring.service.CookingRecipeService
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.readBytes
import io.ktor.client.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal class RecipiesViewModel(context: Context) : AbstractViewModel(context) {

    private val cookingRecipeService = durableRpcService<CookingRecipeService>(CookingRecipeRpcPath)

    fun extractFromImages() {
        val apiKey = store.geminiKey.takeIf { store.useGemini } ?: return
        val userId = store.userId
        viewModelScope.launch {
            val files =
                FileKit.openFilePicker(type = SUPPORTED_IMAGE_FORMATS, mode = FileKitMode.Multiple()) ?: return@launch
            val images = files.map { Image(it.readBytes(), it.extension) }
            val recipes = httpClient.extractRecipes(apiKey, images) ?: return@launch
            coroutineScope {
                recipes.recipes.forEach { recipe ->
                    launch {
                        val ingredients = recipe.items.map { RecipeIngredient(it.name, it.measures, it.unit) }
                        val steps = recipe.steps.map { it.description }
                        cookingRecipeService.durableCall {
                            createCookingRecipe(
                                name = recipe.title,
                                ingredients = ingredients,
                                steps = steps,
                                byUserId = userId,
                            )
                        }
                    }
                }
            }
        }
    }
}

private val SUPPORTED_IMAGE_FORMATS = FileKitType.File("png", "PNG", "jpg", "JPG", "jpeg", "JPEG")

@Serializable
@SerialName("Recipes")
@LLMDescription("Recipe extracted from image")
private data class ExtractedRecipes(
    @property:LLMDescription("List of extracted recipes")
    val recipes: List<ExtractedRecipe>,
)

@Serializable
@SerialName("Recipe")
@LLMDescription("Recipe extracted from image")
private data class ExtractedRecipe(
    @property:LLMDescription("Title for the extracted recipe")
    val title: String,
    @property:LLMDescription("List of extracted ingredients")
    val items: List<ExtractedIngredient>,
    @property:LLMDescription("List of extracted recipe steps")
    val steps: List<ExtractedRecipeStep>,
)

@Serializable
@SerialName("Ingredient")
@LLMDescription("Single ingredient extracted from image")
private data class ExtractedIngredient(
    @property:LLMDescription("Name of the ingredient")
    val name: String,
    @property:LLMDescription("Number of units of ingredient")
    val measures: Double,
    @property:LLMDescription("Unit of ingredient ")
    val unit: String,
)

@Serializable
@SerialName("RecipeStep")
@LLMDescription("Single step of recipe extracted from image")
private data class ExtractedRecipeStep(
    @property:LLMDescription("Actions to be done as a single step of the extracted recipe")
    val description: String,
)


private class Image(
    val bytes: ByteArray,
    val format: String,
)

private suspend fun HttpClient.extractRecipes(
    apiKey: String,
    images: List<Image>,
): ExtractedRecipes? {
    val httpClientFactory = KtorKoogHttpClient.Factory(baseClient = this)
    val googleClient = GoogleLLMClient(apiKey, httpClientFactory = httpClientFactory)
    val promptExecutor = MultiLLMPromptExecutor(googleClient)
    val structuredResponse = promptExecutor.executeStructured(
        prompt = prompt("recipes-extraction") {
            system(
                """
                You are a cooking recipies extraction assistant.
                Extract specific cooking recipes from the user input images.
                For every ingredient, strictly isolate the numerical quantity, 
                the measurement unit, and the ingredient name.
                This separation is mandatory to allow for automated recipe scaling.
                When extracting the preparation and cooking steps, you must refer 
                to the ingredients using the exact names defined in your extracted 
                ingredients list. Do not paraphrase, shorten, or alter the names 
                in the instructions (e.g., if the ingredient list says 
                "unsalted butter", do not write "butter" in the steps).
                Do not include emojis or other non-textual content in the extracted recipe, 
                and if such content is present in input, interpret it accordingly.
                Use the order of user images as a reference for order of steps, 
                if that's not clear from the recipe content.
                Use the same language as the you're given in the user image.
                """.trimIndent(),
            )

            user {
                for ((idx, image) in images.withIndex()) {
                    image(
                        AttachmentSource.Image(
                            content = AttachmentContent.Binary.Bytes(image.bytes),
                            format = image.format,
                            fileName = "recipe_part_${idx + 1}",
                        ),
                    )
                }
            }
        },
        model = GoogleModels.Gemini2_5Flash,
        config = StructuredRequestConfig(
            default = Manual(recipesStructure),
        ),
    )
    return structuredResponse.getOrNull()?.data
}

private val examples: List<ExtractedRecipes> = listOf(
    ExtractedRecipes(
        recipes = listOf(
            ExtractedRecipe(
                title = "Scrambled Eggs",
                items = listOf(
                    ExtractedIngredient(name = "eggs", measures = 3.0, unit = "pcs"),
                    ExtractedIngredient(name = "milk", measures = 50.0, unit = "ml"),
                    ExtractedIngredient(name = "butter", measures = 10.0, unit = "g"),
                    ExtractedIngredient(name = "salt", measures = 1.0, unit = "pinch"),
                ),
                steps = listOf(
                    ExtractedRecipeStep(description = "Whisk the eggs and milk in a bowl with a pinch of salt."),
                    ExtractedRecipeStep(description = "Melt the butter in a pan over medium heat."),
                    ExtractedRecipeStep(description = "Pour in the egg mixture and cook until set, stirring occasionally."),
                ),
            ),
            ExtractedRecipe(
                title = "Spaghetti Pomodoro",
                items = listOf(
                    ExtractedIngredient(name = "spaghetti", measures = 200.0, unit = "g"),
                    ExtractedIngredient(name = "tomato sauce", measures = 400.0, unit = "ml"),
                    ExtractedIngredient(name = "onion", measures = 1.0, unit = "pc"),
                    ExtractedIngredient(name = "olive oil", measures = 2.0, unit = "tbsp"),
                ),
                steps = listOf(
                    ExtractedRecipeStep(description = "Boil the water and cook spaghetti according to package instructions."),
                    ExtractedRecipeStep(description = "Chop the onion and sauté in olive oil until translucent."),
                    ExtractedRecipeStep(description = "Add tomato sauce to the onion and simmer for 10 minutes."),
                    ExtractedRecipeStep(description = "Drain pasta and mix with the sauce."),
                ),
            ),
        ),
    ),
    ExtractedRecipes(
        recipes = listOf(
            ExtractedRecipe(
                title = "Simple Green Salad",
                items = listOf(
                    ExtractedIngredient(name = "lettuce", measures = 1.0, unit = "head"),
                    ExtractedIngredient(name = "cucumber", measures = 0.5, unit = "pc"),
                    ExtractedIngredient(name = "olive oil", measures = 30.0, unit = "ml"),
                    ExtractedIngredient(name = "lemon juice", measures = 1.0, unit = "tbsp"),
                ),
                steps = listOf(
                    ExtractedRecipeStep(description = "Wash and tear the lettuce into bite-sized pieces."),
                    ExtractedRecipeStep(description = "Slice the cucumber and add to the lettuce."),
                    ExtractedRecipeStep(description = "Mix olive oil and lemon juice to create a dressing."),
                    ExtractedRecipeStep(description = "Toss the salad with the dressing."),
                ),
            ),
            ExtractedRecipe(
                title = "Classic Pancakes",
                items = listOf(
                    ExtractedIngredient(name = "flour", measures = 1.5, unit = "cups"),
                    ExtractedIngredient(name = "sugar", measures = 1.0, unit = "tbsp"),
                    ExtractedIngredient(name = "baking powder", measures = 3.5, unit = "tsp"),
                    ExtractedIngredient(name = "milk", measures = 1.25, unit = "cups"),
                ),
                steps = listOf(
                    ExtractedRecipeStep(description = "Sift the flour, sugar, and baking powder into a large bowl."),
                    ExtractedRecipeStep(description = "Make a well in the center and pour in the milk."),
                    ExtractedRecipeStep(description = "Mix until smooth and cook on a griddle."),
                ),
            ),
            ExtractedRecipe(
                title = "Roast Chicken",
                items = listOf(
                    ExtractedIngredient(name = "whole chicken", measures = 1.5, unit = "kg"),
                    ExtractedIngredient(name = "garlic", measures = 4.0, unit = "cloves"),
                    ExtractedIngredient(name = "rosemary", measures = 3.0, unit = "sprigs"),
                    ExtractedIngredient(name = "salt", measures = 1.5, unit = "tsp"),
                ),
                steps = listOf(
                    ExtractedRecipeStep(description = "Rub the whole chicken with salt and crushed garlic cloves."),
                    ExtractedRecipeStep(description = "Place rosemary sprigs inside the cavity."),
                    ExtractedRecipeStep(description = "Roast in the oven at 200°C for 60 minutes."),
                ),
            ),
            ExtractedRecipe(
                title = "Protein Smoothie",
                items = listOf(
                    ExtractedIngredient(name = "protein powder", measures = 1.0, unit = "scoop"),
                    ExtractedIngredient(name = "frozen berries", measures = 1.0, unit = "cup"),
                    ExtractedIngredient(name = "spinach", measures = 1.0, unit = "handful"),
                    ExtractedIngredient(name = "almond milk", measures = 250.0, unit = "ml"),
                ),
                steps = listOf(
                    ExtractedRecipeStep(description = "Combine protein powder, frozen berries, and spinach in a blender."),
                    ExtractedRecipeStep(description = "Add almond milk and blend until smooth."),
                ),
            ),
        ),
    ),
)

private val recipesStructure =
    createMarkdownWrappedJsonStructure<ExtractedRecipes>(examples = examples)
