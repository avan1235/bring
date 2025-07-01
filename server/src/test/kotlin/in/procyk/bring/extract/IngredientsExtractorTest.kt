package `in`.procyk.bring.extract

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal abstract class IngredientsExtractorTest(
    private val extractor: KsoupIngredientsExtractor,
    private val testUrl: String,
    private vararg val ingredients: String
) {
    @Test
    fun `supports test url`() = runTest {
        val supports = extractor.supports(testUrl)

        assertTrue(supports)
    }

    @Test
    fun `extracts some ingredients`() = runTest {
        val ingredients = extractor.extractIngredients(testUrl)

        assertNotEquals(0, ingredients.size, "Expected non empty list of ingredients")

        val expected = this@IngredientsExtractorTest.ingredients.toList()
        val actual = ingredients.map { it.description }
        assertEquals(
            expected = expected,
            actual = actual,
            message =
                "expected following ingredients:\n${expected.joinToString("\",\n\"", "\"", "\"")}\n" +
                        "but got:\n${actual.joinToString("\",\n\"", "\"", "\"")}\n"
        )
    }
}