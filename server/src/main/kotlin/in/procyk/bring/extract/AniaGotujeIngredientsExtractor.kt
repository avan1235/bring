package `in`.procyk.bring.extract

import com.fleeksoft.ksoup.nodes.Document

internal data object AniaGotujeIngredientsExtractor : KsoupIngredientsExtractor() {

    override suspend fun supports(input: String): Boolean {
        return input.startsWith("https://aniagotuje.pl/przepis/")
    }

    override suspend fun extractIngredients(document: Document): List<Ingredient> {
        return document.body()
            .select("#recipeIngredients span[itemprop=\"recipeIngredient\"]")
            .map {
                val ingredient = it.select("span[class=\"ingredient\"]").text()
                val qty = it.select("span[class=\"qty\"]").text()
                Ingredient("$qty $ingredient")
            }
    }
}