package `in`.procyk.bring.test

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.junit.Test
import kotlin.uuid.Uuid

@OptIn(ExperimentalTestApi::class)
internal class LoyaltyCardsScreenScreenshotTest : BringAppCreateScreenshotTest() {

    @Test
    fun `loyalty cards screen`() = screenshotBringApp(
        modifier = Modifier
            .scale(MidScale),
        config = {
            copy(
                themeColor = Color.Red.toArgb(),
                loyaltyCardsIds = setOf(
                    Uuid.parse("002a42bd-2454-4843-ae4c-6cf4346a2dcd"),
                    Uuid.parse("8f33ffbf-d32c-420a-a472-be31a2527b54"),
                    Uuid.parse("3f9f4a91-8862-4289-b8a8-1efe70774bfc"),
                    Uuid.parse("011bcb54-58e9-45bd-ab75-8f11454ddad0"),
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(vertical = 36.dp, horizontal = 18.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BigScreenshotText(
                    text = buildAnnotatedString {
                        append("Keep ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("All")
                        }
                        appendLine(" Loyalty Cards")
                    },
                )
                BigScreenshotText(
                    text = buildAnnotatedString {
                        append("in ")
                        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                            append("single")
                        }
                        append(" place")
                    },
                )
            }
        }
    ) {
        navigateLoyaltyCardsScreen()

        waitUntilNodeCount("loyalty-card", 4)
    }
}