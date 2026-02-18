package `in`.procyk.bring.extract

internal class KwestiaSmakuIngredientsExtractorTest : IngredientsExtractorTest(
    KwestiaSmakuIngredientsExtractor,
    "https://www.kwestiasmaku.com/przepis/sernik-baskijski",
    "1 kg serka kremowego (cream cheese)*",
    "300 g cukru",
    "1 łyżka cukru wanilinowego lub 2 łyżeczki ekstraktu",
    "3 łyżki mąki pszennej",
    "5 jajek",
    "200 g śmietanki 36% lub 30%"
)