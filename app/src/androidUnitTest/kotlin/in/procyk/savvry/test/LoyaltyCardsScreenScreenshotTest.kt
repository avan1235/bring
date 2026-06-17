package `in`.procyk.savvry.test

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
import `in`.procyk.savvry.Code
import `in`.procyk.savvry.LoyaltyCard
import `in`.procyk.savvry.LoyaltyCardData
import org.junit.Test
import kotlin.uuid.Uuid

@OptIn(ExperimentalTestApi::class)
internal class LoyaltyCardsScreenScreenshotTest : SavvryAppCreateScreenshotTest() {

    @Test
    fun `loyalty cards screen`() = screenshotSavvryApp(
        modifier = Modifier
            .scale(MidScale),
        config = {
            copy(
                themeColor = Color.Magenta.toArgb(),
                loyaltyCards =
                    listOf(
                        "76798ee6-6385-4bc6-964a-e6a8a8938981" to "Lidl",
                        "6022262e-b31a-450c-a83b-6c9f93bfa529" to "Biedronka",
                        "8441fed0-b99b-49e9-9cb7-b7e9effb8774" to "H&M",
                    )
                        .mapIndexed { idx, (uuid, label) ->
                            val uuid = Uuid.parse(uuid)
                            LoyaltyCard(
                                cardId = uuid,
                                order = idx.toDouble() + 1.0,
                                cachedData = LoyaltyCardData(
                                    uuid,
                                    label,
                                    Code("", Code.Format.QR_CODE, Code.Bits(0, 0, booleanArrayOf())),
                                ),
                            )
                        },
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
                        append("And ")
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
        },
    ) {
        navigateLoyaltyCardsScreen()

        waitUntilNodeCount("loyalty-card", 3)
    }
}