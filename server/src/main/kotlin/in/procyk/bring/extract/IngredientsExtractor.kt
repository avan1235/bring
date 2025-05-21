package `in`.procyk.bring.extract

import com.fleeksoft.ksoup.nodes.Document

internal interface IngredientsExtractor {

    suspend fun supports(url: String): Boolean

    suspend fun extractIngredients(document: Document): List<Ingredient>

    suspend fun extractTitle(document: Document): String?
}
