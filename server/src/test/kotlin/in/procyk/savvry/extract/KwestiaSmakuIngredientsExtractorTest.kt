package `in`.procyk.savvry.extract

internal class KwestiaSmakuIngredientsExtractorTest : IngredientsExtractorTest(
    KwestiaSmakuIngredientsExtractor,
    "https://www.kwestiasmaku.com/kuchnia_polska/nale%C5%9Bniki/nalesniki.html",
    "1 szklanka mąki pszennej",
    "1 szklanka mleka",
    "3/4 szklanki wody (np. gazowanej) lub mleka",
    "2 duże jajka",
    "szczypta soli",
    "3 łyżki oleju roślinnego lub roztopionego masła",
)