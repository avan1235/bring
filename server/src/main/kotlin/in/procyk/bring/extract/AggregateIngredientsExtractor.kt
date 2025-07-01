package `in`.procyk.bring.extract

internal class AggregateIngredientsExtractor(
    private vararg val extractors: IngredientsExtractor,
) : IngredientsExtractor {

    override suspend fun supports(input: String): Boolean {
        return extractors.any { it.supports(input) }
    }

    override suspend fun extractIngredients(input: String): List<Ingredient> {
        val extractor = extractors.firstOrNull { it.supports(input) } ?: return emptyList()
        return extractor.extractIngredients(input)
    }

    override suspend fun extractTitle(input: String): String? {
        val extractor = extractors.firstOrNull { it.supports(input) } ?: return null
        return extractor.extractTitle(input)
    }
}
