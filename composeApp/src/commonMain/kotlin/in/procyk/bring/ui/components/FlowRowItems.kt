package `in`.procyk.bring.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import `in`.procyk.bring.ui.BringAppTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun <T : Any> FlowRowItems(
    items: List<T>,
    modifier: Modifier = Modifier,
    name: (T) -> String,
    id: (T) -> Any,
    visible: (T) -> Boolean = { true },
    trailingIcon: @Composable (() -> Unit)? = null,
    onClick: (T) -> Unit,
) {
    val visibleItemsIds = remember { mutableStateListOf<Pair<T, Any>>() }
    LaunchedEffect(items) {
        items.forEach { item ->
            val itemId = id(item)
            if (visibleItemsIds.none { it.second == itemId }) visibleItemsIds.add(item to itemId)
        }
    }
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        visibleItemsIds.forEach { (item, itemId) ->
            key(itemId) {
                val itemName = name(item)
                val animationSpecFloat = remember {
                    tween<Float>(durationMillis = (20 * itemName.length).coerceIn(60, 400), easing = LinearEasing)
                }
                val animationSpecIntSize = remember {
                    tween<IntSize>(durationMillis = (20 * itemName.length).coerceIn(60, 400), easing = LinearEasing)
                }
                AnimatedVisibility(
                    visible = item in items && visible(item),
                    enter = fadeIn(animationSpec = animationSpecFloat) + expandHorizontally(
                        animationSpec = animationSpecIntSize,
                        expandFrom = Alignment.CenterHorizontally
                    ),
                    exit = fadeOut(animationSpec = animationSpecFloat) + shrinkHorizontally(
                        animationSpec = animationSpecIntSize,
                        shrinkTowards = Alignment.CenterHorizontally
                    ),
                ) {
                    DisposableEffect(Unit) {
                        onDispose {
                            if (item !in items || !visible(item)) {
                                visibleItemsIds.remove(item to itemId)
                            }
                        }
                    }
                    OutlinedChip(
                        onClick = { onClick(item) },
                        trailingIcon = trailingIcon,
                    ) {
                        Text(
                            text = itemName,
                            style = BringAppTheme.typography.label1,
                        )
                    }
                }
            }
        }
    }
}
