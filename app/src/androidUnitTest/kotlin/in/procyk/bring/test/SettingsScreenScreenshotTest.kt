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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
internal class SettingsScreenScreenshotTest : BringAppCreateScreenshotTest() {

    @Test
    fun `settings screen options`() = screenshotBringApp(
        modifier = Modifier
            .scale(MidScale),
        config = {
            copy(themeColor = Color.Magenta.toArgb())
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(36.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BigScreenshotText(
                    text = buildAnnotatedString {
                        append("Customize ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Your")
                        }
                        append(" app")
                    },
                )
                BigScreenshotText(
                    text = buildAnnotatedString {
                        append("the way ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("You")
                        }
                        append(" want")
                    },
                )
            }
        }
    ) {
        navigateSettingsScreen()
    }
}