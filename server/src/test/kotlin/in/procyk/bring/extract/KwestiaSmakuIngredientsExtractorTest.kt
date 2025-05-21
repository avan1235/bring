package `in`.procyk.bring.extract

import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner

@RunWith(BlockJUnit4ClassRunner::class)
internal class KwestiaSmakuIngredientsExtractorTest : IngredientsExtractorTest(
    KwestiaSmakuIngredientsExtractor,
    "https://www.kwestiasmaku.com/przepis/sernik-baskijski",
    "1 kg serka kremowego (cream cheese, Philadelphia lub Twój Smak)",
    "300 g cukru",
    "1 łyżka cukru wanilinowego lub 2 łyżeczki ekstraktu",
    "3 łyżki mąki pszennej",
    "5 jajek",
    "250 g śmietanki 30%"
)