package `in`.procyk.bring.extract

import com.fleeksoft.ksoup.nodes.Document

internal class AggregateIngredientsExtractor(
    private vararg val extractors: IngredientsExtractor,
) : IngredientsExtractor {

    override suspend fun supports(url: String): Boolean {
        return extractors.any { it.supports(url) }
    }

    override suspend fun extractIngredients(document: Document): List<Ingredient> {
        val url = document.baseUri()
        val extractor = extractors.firstOrNull { it.supports(url) } ?: return emptyList()
        return extractor.extractIngredients(document)
    }

    override suspend fun extractTitle(document: Document): String? {
        val url = document.baseUri()
        val extractor = extractors.firstOrNull { it.supports(url) } ?: return null
        return extractor.extractTitle(document)
    }
}
