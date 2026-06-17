package `in`.procyk.savvry.extract

internal interface IngredientsExtractor {

    suspend fun supports(input: String): Boolean

    suspend fun extractIngredients(input: String): List<Ingredient>

    suspend fun extractTitle(input: String): String?
}
