package `in`.procyk.bring.extract

import com.fleeksoft.ksoup.nodes.Document

internal data object CookidooIngredientsExtractor : KsoupIngredientsExtractor() {

    override suspend fun supports(input: String): Boolean {
        return input.startsWith("https://cookidoo.pl/recipes/recipe/")
    }

    override suspend fun extractIngredients(document: Document): List<Ingredient> {
        return document.body()
            .select("div[class=\"recipe-ingredient__wrapper\"]")
            .map {
                val name = it.select("span[class=\"recipe-ingredient__name\"]").text()
                val amount = it.select("span[class=\"recipe-ingredient__amount\"]").text()
                Ingredient("$amount $name")
            }
    }
}