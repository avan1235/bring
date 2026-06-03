package `in`.procyk.bring.extract

internal class KwestiaSmakuIngredientsExtractorTest : IngredientsExtractorTest(
    KwestiaSmakuIngredientsExtractor,
    "https://www.kwestiasmaku.com/przepis/sernik-baskijski",
    "150 g herbatników (np. petit beurre, holenderskie)",
    "50 g masła",
    "1 kg solonego serka kremowego (cream cheese)*",
    "250 g cukru",
    "1 łyżka cukru wanilinowego lub 2 łyżeczki ekstraktu",
    "3 łyżki mąki pszennej",
    "5 jajek",
    "200 g śmietanki 30%"
)