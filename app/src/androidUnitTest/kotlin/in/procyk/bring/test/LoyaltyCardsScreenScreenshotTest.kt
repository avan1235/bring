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
import `in`.procyk.bring.LoyaltyCard
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
                loyaltyCards = "76798ee6-6385-4bc6-964a-e6a8a8938981;6022262e-b31a-450c-a83b-6c9f93bfa529;8441fed0-b99b-49e9-9cb7-b7e9effb8774"
                    .split(';')
                    .mapIndexed { idx, uuid ->
                        LoyaltyCard(
                            Uuid.parse(uuid),
                            idx.toDouble() + 1.0,
                        )
                    }
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

        waitUntilNodeCount("loyalty-card", 3)
    }
}