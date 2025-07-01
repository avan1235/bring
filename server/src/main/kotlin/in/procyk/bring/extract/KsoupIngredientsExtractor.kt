package `in`.procyk.bring.extract

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.network.parseGetRequest
import com.fleeksoft.ksoup.nodes.Document

internal abstract class KsoupIngredientsExtractor : IngredientsExtractor {

    override suspend fun extractTitle(input: String): String? {
        val document = Ksoup.parseGetRequest(input)
        return document.title().takeUnless { it.isBlank() }
    }

    final override suspend fun extractIngredients(input: String): List<Ingredient> {
        val document = Ksoup.parseGetRequest(input)
        return extractIngredients(document)
    }

    protected abstract suspend fun extractIngredients(document: Document): List<Ingredient>
}