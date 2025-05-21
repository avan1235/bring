package `in`.procyk.bring.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
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
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.ui.LocalContentColor
import `in`.procyk.bring.ui.foundation.ButtonElevation

@Composable
internal fun IconButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    variant: IconButtonVariant = IconButtonVariant.Primary,
    shape: Shape = IconButtonDefaults.ButtonSquareShape,
    onClick: () -> Unit = {},
    contentPadding: PaddingValues = IconButtonDefaults.ButtonPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    val style = IconButtonDefaults.styleFor(variant, shape)

    IconButtonComponent(
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        style = style,
        onClick = onClick,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}

@Composable
private fun IconButtonComponent(
    modifier: Modifier,
    enabled: Boolean,
    loading: Boolean,
    style: IconButtonStyle,
    onClick: () -> Unit,
    contentPadding: PaddingValues,
    interactionSource: MutableInteractionSource,
    content: @Composable () -> Unit,
) {
    val containerColor = style.colors.containerColor(enabled).value
    val contentColor = style.colors.contentColor(enabled).value
    val borderColor = style.colors.borderColor(enabled).value
    val borderStroke = if (borderColor != null) BorderStroke(IconButtonDefaults.OutlineHeight, borderColor) else null

    val shadowElevation = style.elevation?.shadowElevation(enabled, interactionSource)?.value ?: 0.dp

    Surface(
        onClick = onClick,
        modifier =
            modifier.defaultMinSize(
                minWidth = IconButtonDefaults.ButtonSize,
                minHeight = IconButtonDefaults.ButtonSize,
            ).semantics { role = Role.Button },
        enabled = enabled,
        shape = style.shape,
        color = containerColor,
        contentColor = contentColor,
        border = borderStroke,
        shadowElevation = shadowElevation,
        interactionSource = interactionSource,
    ) {
        Box(
            modifier = Modifier.padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            // Add a loading indicator if needed
            content()
        }
    }
}

internal fun Modifier.compactButtonMinSize(): Modifier = defaultMinSize(
    minWidth = 36.dp,
    minHeight = 36.dp,
)

internal enum class IconButtonVariant {
    Primary,
    PrimaryOutlined,
    PrimaryElevated,
    PrimaryGhost,
    Secondary,
    SecondaryOutlined,
    SecondaryElevated,
    SecondaryGhost,
    Destructive,
    DestructiveOutlined,
    DestructiveElevated,
    DestructiveGhost,
    Ghost,
}

internal object IconButtonDefaults {
    val ButtonSize = 44.dp
    val ButtonPadding = PaddingValues(4.dp)
    val ButtonSquareShape = RoundedCornerShape(12.dp)
    val ButtonCircleShape = RoundedCornerShape(percent = 50)
    val OutlineHeight = 1.dp

    @Composable
    fun buttonElevation() =
        ButtonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 2.dp,
            focusedElevation = 2.dp,
            hoveredElevation = 2.dp,
            disabledElevation = 0.dp,
        )

    @Composable
    fun styleFor(variant: IconButtonVariant, shape: Shape): IconButtonStyle {
        return when (variant) {
            IconButtonVariant.Primary -> primaryFilled(shape)
            IconButtonVariant.PrimaryOutlined -> primaryOutlined(shape)
            IconButtonVariant.PrimaryElevated -> primaryElevated(shape)
            IconButtonVariant.PrimaryGhost -> primaryGhost(shape)
            IconButtonVariant.Secondary -> secondaryFilled(shape)
            IconButtonVariant.SecondaryOutlined -> secondaryOutlined(shape)
            IconButtonVariant.SecondaryElevated -> secondaryElevated(shape)
            IconButtonVariant.SecondaryGhost -> secondaryGhost(shape)
            IconButtonVariant.Destructive -> destructiveFilled(shape)
            IconButtonVariant.DestructiveOutlined -> destructiveOutlined(shape)
            IconButtonVariant.DestructiveElevated -> destructiveElevated(shape)
            IconButtonVariant.DestructiveGhost -> destructiveGhost(shape)
            IconButtonVariant.Ghost -> ghost(shape)
        }
    }

    @Composable
    fun primaryFilled(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = BringAppTheme.colors.primary,
                    contentColor = BringAppTheme.colors.onPrimary,
                    disabledContainerColor = BringAppTheme.colors.disabled,
                    disabledContentColor = BringAppTheme.colors.onDisabled,
                ),
            shape = shape,
            elevation = null,
        )

    @Composable
    fun primaryOutlined(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = BringAppTheme.colors.transparent,
                    contentColor = BringAppTheme.colors.primary,
                    borderColor = BringAppTheme.colors.primary,
                    disabledContainerColor = BringAppTheme.colors.transparent,
                    disabledContentColor = BringAppTheme.colors.onDisabled,
                    disabledBorderColor = BringAppTheme.colors.disabled,
                ),
            shape = shape,
            elevation = null,
        )

    @Composable
    fun primaryElevated(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = BringAppTheme.colors.primary,
                    contentColor = BringAppTheme.colors.onPrimary,
                    disabledContainerColor = BringAppTheme.colors.disabled,
                    disabledContentColor = BringAppTheme.colors.onDisabled,
                ),
            shape = shape,
            elevation = buttonElevation(),
        )

    @Composable
    fun primaryGhost(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = BringAppTheme.colors.transparent,
                    contentColor = BringAppTheme.colors.primary,
                    borderColor = BringAppTheme.colors.transparent,
                    disabledContainerColor = BringAppTheme.colors.transparent,
                    disabledContentColor = BringAppTheme.colors.onDisabled,
                    disabledBorderColor = BringAppTheme.colors.transparent,
                ),
            shape = shape,
            elevation = null,
        )

    @Composable
    fun secondaryFilled(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = BringAppTheme.colors.secondary,
                    contentColor = BringAppTheme.colors.onSecondary,
                    disabledContainerColor = BringAppTheme.colors.disabled,
                    disabledContentColor = BringAppTheme.colors.onDisabled,
                ),
            shape = shape,
            elevation = null,
        )

    @Composable
    fun secondaryOutlined(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = BringAppTheme.colors.transparent,
                    contentColor = BringAppTheme.colors.secondary,
                    borderColor = BringAppTheme.colors.secondary,
                    disabledContainerColor = BringAppTheme.colors.transparent,
                    disabledContentColor = BringAppTheme.colors.onDisabled,
                    disabledBorderColor = BringAppTheme.colors.disabled,
                ),
            shape = shape,
            elevation = null,
        )

    @Composable
    fun secondaryElevated(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = BringAppTheme.colors.secondary,
                    contentColor = BringAppTheme.colors.onSecondary,
                    disabledContainerColor = BringAppTheme.colors.disabled,
                    disabledContentColor = BringAppTheme.colors.onDisabled,
                ),
            shape = shape,
            elevation = buttonElevation(),
        )

    @Composable
    fun secondaryGhost(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = BringAppTheme.colors.transparent,
                    contentColor = BringAppTheme.colors.secondary,
                    borderColor = BringAppTheme.colors.transparent,
                    disabledContainerColor = BringAppTheme.colors.transparent,
                    disabledContentColor = BringAppTheme.colors.onDisabled,
                    disabledBorderColor = BringAppTheme.colors.transparent,
                ),
            shape = shape,
            elevation = null,
        )

    @Composable
    fun destructiveFilled(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = BringAppTheme.colors.error,
                    contentColor = BringAppTheme.colors.onError,
                    disabledContainerColor = BringAppTheme.colors.disabled,
                    disabledContentColor = BringAppTheme.colors.onDisabled,
                ),
            shape = shape,
            elevation = null,
        )

    @Composable
    fun destructiveOutlined(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = BringAppTheme.colors.transparent,
                    contentColor = BringAppTheme.colors.error,
                    borderColor = BringAppTheme.colors.error,
                    disabledContainerColor = BringAppTheme.colors.transparent,
                    disabledContentColor = BringAppTheme.colors.onDisabled,
                    disabledBorderColor = BringAppTheme.colors.disabled,
                ),
            shape = shape,
            elevation = null,
        )

    @Composable
    fun destructiveElevated(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = BringAppTheme.colors.error,
                    contentColor = BringAppTheme.colors.onError,
                    disabledContainerColor = BringAppTheme.colors.disabled,
                    disabledContentColor = BringAppTheme.colors.onDisabled,
                ),
            shape = shape,
            elevation = buttonElevation(),
        )

    @Composable
    fun destructiveGhost(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = BringAppTheme.colors.transparent,
                    contentColor = BringAppTheme.colors.error,
                    borderColor = BringAppTheme.colors.transparent,
                    disabledContainerColor = BringAppTheme.colors.transparent,
                    disabledContentColor = BringAppTheme.colors.onDisabled,
                    disabledBorderColor = BringAppTheme.colors.transparent,
                ),
            shape = shape,
            elevation = null,
        )

    @Composable
    fun ghost(shape: Shape) =
        IconButtonStyle(
            colors =
                IconButtonColors(
                    containerColor = BringAppTheme.colors.transparent,
                    contentColor = LocalContentColor.current,
                    disabledContainerColor = BringAppTheme.colors.transparent,
                    disabledContentColor = BringAppTheme.colors.onDisabled,
                ),
            shape = shape,
            elevation = null,
        )
}

@Immutable
data class IconButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color? = null,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val disabledBorderColor: Color? = null,
) {
    @Composable
    fun containerColor(enabled: Boolean) = rememberUpdatedState(if (enabled) containerColor else disabledContainerColor)

    @Composable
    fun contentColor(enabled: Boolean) = rememberUpdatedState(if (enabled) contentColor else disabledContentColor)

    @Composable
    fun borderColor(enabled: Boolean) = rememberUpdatedState(if (enabled) borderColor else disabledBorderColor)
}

@Immutable
data class IconButtonStyle(
    val colors: IconButtonColors,
    val shape: Shape,
    val elevation: ButtonElevation? = null,
)
