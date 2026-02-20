package `in`.procyk.bring.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.GestureEnd
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.GestureThresholdActivate
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import `in`.procyk.bring.LocalBringStore
import `in`.procyk.bring.ui.BringAppTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState

@Composable
internal fun LazyItemScope.ReorderableItemRow(
    state: ReorderableLazyListState,
    key: Any,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    animateItemModifier: Modifier = Modifier.animateItem(),
    content: @Composable RowScope.(isDragging: Boolean) -> Unit,
) {
    ReorderableItem(state, key, modifier, enabled, animateItemModifier) { isDragging ->
        val haptics = LocalHapticFeedback.current
        val useHaptics = LocalBringStore.current.useHaptics
        LaunchedEffect(isDragging) {
            when {
                !useHaptics -> return@LaunchedEffect
                isDragging -> haptics.performHapticFeedback(GestureThresholdActivate)
                else -> haptics.performHapticFeedback(GestureEnd)
            }
        }
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.background(BringAppTheme.colors.surface).fillMaxWidth()
        ) {
            IconButton(
                modifier = Modifier.draggableHandle().compactButtonMinSize(),
                variant = IconButtonVariant.Ghost,
                contentPadding = CompactButtonPadding,
            ) {
                Icon(Icons.Rounded.DragHandle)
            }
            content(isDragging)
        }
    }
}

internal val CompactButtonPadding = PaddingValues(0.dp)