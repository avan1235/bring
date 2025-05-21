package `in`.procyk.bring.extract

import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner

@RunWith(BlockJUnit4ClassRunner::class)
internal class AniaGotujeIngredientsExtractorTest : IngredientsExtractorTest(
    AniaGotujeIngredientsExtractor,
    "https://aniagotuje.pl/przepis/biala-kielbasa-w-sosie-chrzanowym",
    "600 g - 8 małych sztuk biała kiełbasa - u mnie parzona",
    "125 ml - pół szklanki śmietanka kremówka 30 %",
    "80 g majonez",
    "50 g chrzan tarty",
    "200 g - 1 większa cebula",
    "2 łyżki olej roślinny do smażenia",
    "1 łyżka majeranek",
    "po 1/3 płaskiej łyżeczki sól i pieprz",
    "do dekoracji natka pietruszki"
)