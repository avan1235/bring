package `in`.procyk.bring.extract

import com.fleeksoft.ksoup.nodes.Document

abstract class KsoupIngredientsExtractor : IngredientsExtractor {

    override suspend fun extractTitle(document: Document): String? =
        document.title().takeUnless { it.isBlank() }
}