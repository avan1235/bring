package `in`.procyk.bring.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.ui.LocalTextStyle

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
            for (item in items) {
                IconButton(
                    onClick = { uriHandler.openUri(item.url) },
                    variant = IconButtonVariant.SecondaryGhost,
                ) {
                    Icon(item.icon)
                }
            }
        }
    }
}
