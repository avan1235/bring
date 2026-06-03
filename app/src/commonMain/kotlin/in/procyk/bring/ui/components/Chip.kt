package `in`.procyk.bring.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import `in`.procyk.bring.LocalBringStore
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.ui.components.ChipDefaults.ChipIconHorizontalPadding
import `in`.procyk.bring.ui.components.ChipDefaults.ChipIconSize
import `in`.procyk.bring.ui.components.ChipDefaults.ChipRectShape
import `in`.procyk.bring.ui.foundation.ButtonElevation

@Composable
internal fun Chip(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    contentPadding: PaddingValues = ChipDefaults.contentPadding,
    shape: Shape = ChipRectShape,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    label: @Composable (() -> Unit),
) {
    ChipComponent(
        modifier = modifier,
        enabled = enabled,
        selected = selected,
        style = ChipDefaults.primaryFilled(shape),
        onClick = onClick,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        label = label,
    )
}

@Composable
internal fun ElevatedChip(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    contentPadding: PaddingValues = ChipDefaults.contentPadding,
    shape: Shape = ChipRectShape,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    label: @Composable (() -> Unit),
) {
    ChipComponent(
        modifier = modifier,
        enabled = enabled,
        selected = selected,
        style = ChipDefaults.primaryElevated(shape),
        onClick = onClick,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        label = label,
    )
}

@Composable
internal fun OutlinedChip(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    contentPadding: PaddingValues = ChipDefaults.contentPadding,
    shape: Shape = ChipRectShape,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    label: @Composable (() -> Unit),
) {
    ChipComponent(
        modifier = modifier,
        enabled = enabled,
        selected = selected,
        style = ChipDefaults.primaryOutlined(shape),
        onClick = onClick,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        label = label,
    )
}

@Composable
private fun ChipComponent(
    modifier: Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    style: ChipStyle,
    onClick: () -> Unit,
    contentPadding: PaddingValues = ChipDefaults.contentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    label: @Composable () -> Unit,
) {
    val containerColor = style.colors.containerColor(enabled, selected).value
    val contentColor = style.colors.contentColor(enabled, selected).value
    val borderColor = style.colors.borderColor(enabled, selected).value
    val borderStroke =
        if (borderColor != null) {
            BorderStroke(
                ChipDefaults.ChipOutlineHeight,
                borderColor,
            )
        } else {
            null
        }

    val shadowElevation = style.elevation?.shadowElevation(enabled, interactionSource)?.value ?: 0.dp

    Surface(
        onClick = LocalBringStore.current.onClickWithHaptics(onClick),
        modifier = modifier.semantics { role = Role.Button },
        enabled = enabled,
        shape = style.shape,
        color = containerColor,
        contentColor = contentColor,
        border = borderStroke,
        shadowElevation = shadowElevation,
        interactionSource = interactionSource,
    ) {
        DefaultChipComponent(
            modifier = Modifier.padding(contentPadding),
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            label = label,
        )
    }
}

@Composable
private fun DefaultChipComponent(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    label: @Composable (() -> Unit),
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingIcon?.let { icon ->
            Box(
                modifier =
                    Modifier
                        .padding(end = ChipIconHorizontalPadding)
                        .requiredSize(ChipIconSize),
            ) {
                icon.invoke()
            }
        }

        label.invoke()

        trailingIcon?.let { icon ->
            Box(
                modifier =
                    Modifier
                        .padding(start = ChipIconHorizontalPadding)
                        .requiredSize(ChipIconSize),
            ) {
                icon.invoke()
            }
        }
    }
}

internal object ChipDefaults {
    private val ChipPaddingHorizontal = 6.dp
    private val ChipPaddingVertical = 6.dp
    val ChipRectShape = RoundedCornerShape(12)
    val ChipOutlineHeight = 1.dp
    val ChipIconHorizontalPadding = 6.dp
    val ChipIconSize = 16.dp

    val contentPadding =
        PaddingValues(
            start = ChipPaddingHorizontal,
            end = ChipPaddingHorizontal,
            top = ChipPaddingVertical,
            bottom = ChipPaddingVertical,
        )

    @Composable
    fun chipElevation() =
        ButtonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 4.dp,
            focusedElevation = 4.dp,
            hoveredElevation = 4.dp,
            disabledElevation = 0.dp,
        )

    @Composable
    fun primaryFilled(shape: Shape) =
        ChipStyle(
            colors =
                ChipColors(
                    containerColor = BringAppTheme.colors.surface,
                    contentColor = BringAppTheme.colors.onSurface,
                    selectedContainerColor = BringAppTheme.colors.primary,
                    selectedContentColor = BringAppTheme.colors.onPrimary,
                    disabledContainerColor = BringAppTheme.colors.disabled,
                    disabledContentColor = BringAppTheme.colors.onDisabled,
                ),
            shape = shape,
            elevation = null,
            contentPadding = contentPadding,
        )

    @Composable
    fun primaryElevated(shape: Shape) =
        ChipStyle(
            colors =
                ChipColors(
                    containerColor = BringAppTheme.colors.surface,
                    contentColor = BringAppTheme.colors.onSurface,
                    selectedContainerColor = BringAppTheme.colors.primary,
                    selectedContentColor = BringAppTheme.colors.onPrimary,
                    disabledContainerColor = BringAppTheme.colors.disabled,
                    disabledContentColor = BringAppTheme.colors.onDisabled,
                ),
            shape = shape,
            elevation = chipElevation(),
            contentPadding = contentPadding,
        )

    @Composable
    fun primaryOutlined(shape: Shape) =
        ChipStyle(
            colors =
                ChipColors(
                    containerColor = BringAppTheme.colors.transparent,
                    contentColor = BringAppTheme.colors.primary,
                    outlineColor = BringAppTheme.colors.primary,
                    selectedContainerColor = BringAppTheme.colors.primary,
                    selectedOutlineColor = BringAppTheme.colors.primary,
                    selectedContentColor = BringAppTheme.colors.onPrimary,
                    disabledContainerColor = BringAppTheme.colors.transparent,
                    disabledContentColor = BringAppTheme.colors.onDisabled,
                    disabledOutlineColor = BringAppTheme.colors.disabled,
                ),
            shape = shape,
            elevation = null,
            contentPadding = contentPadding,
        )
}

@Immutable
internal data class ChipColors(
    val containerColor: Color,
    val contentColor: Color,
    val outlineColor: Color? = null,
    val selectedContainerColor: Color,
    val selectedOutlineColor: Color? = null,
    val selectedContentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val disabledOutlineColor: Color? = null,
) {
    @Composable
    internal fun containerColor(enabled: Boolean, selected: Boolean) =
        rememberUpdatedState(
            newValue =
                when {
                    !enabled -> disabledContainerColor
                    selected -> selectedContainerColor
                    else -> containerColor
                },
        )

    @Composable
    internal fun contentColor(enabled: Boolean, selected: Boolean) =
        rememberUpdatedState(
            newValue =
                when {
                    !enabled -> disabledContentColor
                    selected -> selectedContentColor
                    else -> contentColor
                },
        )

    @Composable
    fun borderColor(enabled: Boolean, selected: Boolean) =
        rememberUpdatedState(
            newValue =
                when {
                    !enabled -> disabledOutlineColor
                    selected -> selectedOutlineColor
                    else -> outlineColor
                },
        )
}

@Immutable
internal data class ChipStyle(
    val colors: ChipColors,
    val shape: Shape,
    val elevation: ButtonElevation? = null,
    val contentPadding: PaddingValues,
)
