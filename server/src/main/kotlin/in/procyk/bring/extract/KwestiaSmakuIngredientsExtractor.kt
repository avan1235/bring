package `in`.procyk.bring.extract

import com.fleeksoft.ksoup.nodes.Document

internal data object KwestiaSmakuIngredientsExtractor : KsoupIngredientsExtractor() {

    override suspend fun supports(url: String): Boolean {
        return url.startsWith("https://www.kwestiasmaku.com/przepis/")
    }

    override suspend fun extractIngredients(document: Document): List<Ingredient> {
        return document.body()
            .select("div[class=\"field field-name-field-skladniki field-type-text-long field-label-hidden\"]")
            .firstOrNull()
            ?.select("li")
            ?.map { Ingredient(it.text()) }
            .orEmpty()
    }
}