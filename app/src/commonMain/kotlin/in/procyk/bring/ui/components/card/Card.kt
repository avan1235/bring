package `in`.procyk.bring.ui.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.ui.components.Surface

@Composable
internal fun Card(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.Shape,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(),
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = colors.containerColor(enabled = true).value,
        contentColor = colors.contentColor(enabled = true).value,
        shadowElevation =
            elevation.shadowElevation(
                enabled = true,
                interactionSource = null,
            ).value,
        border = border,
    ) {
        Column(content = content)
    }
}

@Composable
internal fun Card(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CardDefaults.Shape,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(),
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        color = colors.containerColor(enabled).value,
        contentColor = colors.contentColor(enabled).value,
        shadowElevation = elevation.shadowElevation(enabled, interactionSource).value,
        border = border,
        interactionSource = interactionSource,
    ) {
        Column(content = content)
    }
}

@Composable
internal fun ElevatedCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.ElevatedShape,
    colors: CardColors = CardDefaults.elevatedCardColors(),
    elevation: CardElevation = CardDefaults.elevatedCardElevation(),
    content: @Composable ColumnScope.() -> Unit,
) = Card(
    modifier = modifier,
    shape = shape,
    border = null,
    elevation = elevation,
    colors = colors,
    content = content,
)

@Composable
internal fun ElevatedCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CardDefaults.ElevatedShape,
    colors: CardColors = CardDefaults.elevatedCardColors(),
    elevation: CardElevation = CardDefaults.elevatedCardElevation(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable ColumnScope.() -> Unit,
) = Card(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = shape,
    colors = colors,
    elevation = elevation,
    border = null,
    interactionSource = interactionSource,
    content = content,
)

@Composable
internal fun OutlinedCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.OutlinedShape,
    colors: CardColors = CardDefaults.outlinedCardColors(),
    elevation: CardElevation = CardDefaults.outlinedCardElevation(),
    border: BorderStroke = CardDefaults.outlinedCardBorder(),
    content: @Composable ColumnScope.() -> Unit,
) = Card(
    modifier = modifier,
    shape = shape,
    border = border,
    elevation = elevation,
    colors = colors,
    content = content,
)

@Composable
internal fun OutlinedCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CardDefaults.OutlinedShape,
    colors: CardColors = CardDefaults.outlinedCardColors(),
    elevation: CardElevation = CardDefaults.outlinedCardElevation(),
    border: BorderStroke = CardDefaults.outlinedCardBorder(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable ColumnScope.() -> Unit,
) = Card(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = shape,
    colors = colors,
    elevation = elevation,
    border = border,
    interactionSource = interactionSource,
    content = content,
)

internal object CardDefaults {
    val Shape: Shape @Composable get() = RoundedCornerShape(12.0.dp)
    val ElevatedShape: Shape @Composable get() = Shape
    val OutlinedShape: Shape @Composable get() = Shape
    private val BorderWidth = 1.dp

    @Composable
    fun cardElevation(
        defaultElevation: Dp = 0.0.dp,
        pressedElevation: Dp = 0.0.dp,
        focusedElevation: Dp = 0.0.dp,
        hoveredElevation: Dp = 1.0.dp,
        draggedElevation: Dp = 3.0.dp,
        disabledElevation: Dp = 0.0.dp,
    ): CardElevation =
        CardElevation(
            defaultElevation = defaultElevation,
            pressedElevation = pressedElevation,
            focusedElevation = focusedElevation,
            hoveredElevation = hoveredElevation,
            draggedElevation = draggedElevation,
            disabledElevation = disabledElevation,
        )

    @Composable
    fun elevatedCardElevation(
        defaultElevation: Dp = 2.0.dp,
        pressedElevation: Dp = 4.0.dp,
        focusedElevation: Dp = 4.0.dp,
        hoveredElevation: Dp = 4.0.dp,
        draggedElevation: Dp = 4.0.dp,
        disabledElevation: Dp = 0.0.dp,
    ): CardElevation =
        CardElevation(
            defaultElevation = defaultElevation,
            pressedElevation = pressedElevation,
            focusedElevation = focusedElevation,
            hoveredElevation = hoveredElevation,
            draggedElevation = draggedElevation,
            disabledElevation = disabledElevation,
        )

    @Composable
    fun outlinedCardElevation(
        defaultElevation: Dp = 0.0.dp,
        pressedElevation: Dp = 0.0.dp,
        focusedElevation: Dp = 0.0.dp,
        hoveredElevation: Dp = 1.0.dp,
        draggedElevation: Dp = 3.0.dp,
        disabledElevation: Dp = 0.0.dp,
    ): CardElevation =
        CardElevation(
            defaultElevation = defaultElevation,
            pressedElevation = pressedElevation,
            focusedElevation = focusedElevation,
            hoveredElevation = hoveredElevation,
            draggedElevation = draggedElevation,
            disabledElevation = disabledElevation,
        )

    @Composable
    fun cardColors(
        containerColor: Color = BringAppTheme.colors.surface,
        contentColor: Color = BringAppTheme.colors.onSurface,
        disabledContainerColor: Color =
            BringAppTheme.colors.disabled,
        disabledContentColor: Color = BringAppTheme.colors.onDisabled,
    ): CardColors =
        CardColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
        )

    @Composable
    fun elevatedCardColors(
        containerColor: Color = BringAppTheme.colors.background,
        contentColor: Color = BringAppTheme.colors.onBackground,
        disabledContainerColor: Color =
            BringAppTheme.colors.disabled,
        disabledContentColor: Color = BringAppTheme.colors.onDisabled,
    ): CardColors =
        CardColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
        )

    @Composable
    fun outlinedCardColors(
        containerColor: Color = BringAppTheme.colors.surface,
        contentColor: Color = BringAppTheme.colors.onSurface,
        disabledContainerColor: Color =
            BringAppTheme.colors.disabled,
        disabledContentColor: Color = BringAppTheme.colors.onDisabled,
    ): CardColors =
        CardColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
        )

    @Composable
    fun outlinedCardBorder(enabled: Boolean = true): BorderStroke {
        val color =
            if (enabled) {
                BringAppTheme.colors.outline
            } else {
                BringAppTheme.colors.disabled
            }
        return remember(color) { BorderStroke(BorderWidth, color) }
    }
}

@ConsistentCopyVisibility
@Immutable
internal data class CardColors internal constructor(
    private val containerColor: Color,
    private val contentColor: Color,
    private val disabledContainerColor: Color,
    private val disabledContentColor: Color,
) {
    @Composable
    internal fun containerColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(if (enabled) containerColor else disabledContainerColor)
    }

    @Composable
    internal fun contentColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(if (enabled) contentColor else disabledContentColor)
    }
}
