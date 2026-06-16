package `in`.procyk.bring.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import `in`.procyk.bring.runIfNotNull

@Composable
internal inline fun RowScope.AnimatedVisibilityGhostButton(
    icon: ImageVector,
    testTag: String? = null,
    visible: Boolean = true,
    enabled: Boolean = true,
    crossinline onClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandHorizontally(),
        exit = fadeOut() + shrinkHorizontally(),
    ) {
        IconButton(
            enabled = enabled,
            variant = IconButtonVariant.Ghost,
            onClick = { onClick() },
            modifier = Modifier
                .compactButtonMinSize()
                .runIfNotNull(testTag) {
                    testTag(it)
                },
            contentPadding = CompactButtonPadding,
        ) {
            Icon(icon)
        }
    }
}