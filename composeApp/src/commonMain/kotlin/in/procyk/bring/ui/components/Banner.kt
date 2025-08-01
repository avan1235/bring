package `in`.procyk.bring.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.ContextClick
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp

data class BottomBannerItem(
    val url: String,
    val icon: ImageVector,
)

@Composable
internal fun BottomBanner(
    title: String?,
    vararg items: BottomBannerItem,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        title?.let { Text(it) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            val uriHandler = LocalUriHandler.current
            val haptics = LocalHapticFeedback.current
            for (item in items) {
                IconButton(
                    onClick = {
                        uriHandler.openUri(item.url)
                        haptics.performHapticFeedback(ContextClick)
                    },
                    variant = IconButtonVariant.SecondaryGhost,
                ) {
                    Icon(item.icon)
                }
            }
        }
    }
}
