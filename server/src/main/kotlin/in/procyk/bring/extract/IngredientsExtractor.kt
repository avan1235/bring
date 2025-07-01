package `in`.procyk.bring.extract

internal interface IngredientsExtractor {

    suspend fun supports(input: String): Boolean

    suspend fun extractIngredients(input: String): List<Ingredient>

    suspend fun extractTitle(input: String): String?
}
