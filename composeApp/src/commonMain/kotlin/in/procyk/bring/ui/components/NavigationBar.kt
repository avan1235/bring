package `in`.procyk.bring.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.procyk.bring.LocalBringStore
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.ui.LocalContentColor
import `in`.procyk.bring.ui.components.NavigationBarDefaults.NavigationBarHeight
import `in`.procyk.bring.ui.components.NavigationBarItemDefaults.ItemAnimationDurationMillis
import `in`.procyk.bring.ui.components.NavigationBarItemDefaults.NavigationBarItemHorizontalPadding
import `in`.procyk.bring.ui.components.NavigationBarItemDefaults.NavigationBarItemVerticalPadding
import `in`.procyk.bring.ui.contentColorFor
import `in`.procyk.bring.ui.foundation.ProvideTextStyle
import kotlin.math.roundToInt

@Composable
internal fun NavigationBar(
    modifier: Modifier = Modifier,
    containerColor: Color = NavigationBarDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        modifier = modifier,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(windowInsets)
                    .height(NavigationBarHeight)
                    .selectableGroup(),
            content = content,
        )
    }
}

@Composable
internal fun RowScope.NavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    alwaysShowLabel: Boolean = true,
    colors: NavigationBarItemColors = NavigationBarItemDefaults.colors(),
    textStyle: TextStyle = NavigationBarItemDefaults.textStyle(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val styledIcon = @Composable {
        val iconColor by colors.iconColor(selected = selected, enabled = enabled)
        val clearSemantics = label != null && (alwaysShowLabel || selected)
        Box(modifier = if (clearSemantics) Modifier.clearAndSetSemantics {} else Modifier) {
            CompositionLocalProvider(LocalContentColor provides iconColor, content = icon)
        }
    }

    val styledLabel: @Composable (() -> Unit)? =
        label?.let {
            @Composable {
                val textColor by colors.textColor(selected = selected, enabled = enabled)
                CompositionLocalProvider(LocalContentColor provides textColor) {
                    ProvideTextStyle(textStyle, content = label)
                }
            }
        }

    var itemWidth by remember { mutableIntStateOf(0) }

    Box(
        modifier
            .selectable(
                selected = selected,
                onClick = LocalBringStore.current.onClickWithHaptics(onClick),
                enabled = enabled,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null,
            )
            .semantics {
                role = Role.Tab
            }
            .weight(1f)
            .onSizeChanged {
                itemWidth = it.width
            },
        contentAlignment = Alignment.Center,
    ) {
        val animationProgress: Float by animateFloatAsState(
            targetValue = if (selected) 1f else 0f,
            animationSpec = tween(ItemAnimationDurationMillis),
        )

        NavigationBarItemBaselineLayout(
            icon = styledIcon,
            label = styledLabel,
            alwaysShowLabel = alwaysShowLabel,
            animationProgress = animationProgress,
        )
    }
}

@Composable
private fun NavigationBarItemBaselineLayout(
    icon: @Composable () -> Unit,
    label: @Composable (() -> Unit)?,
    alwaysShowLabel: Boolean,
    animationProgress: Float,
) {
    Layout({
        Box(Modifier.layoutId(IconLayoutIdTag)) { icon() }

        if (label != null) {
            Box(
                Modifier
                    .layoutId(LabelLayoutIdTag)
                    .alpha(if (alwaysShowLabel) 1f else animationProgress)
                    .padding(horizontal = NavigationBarItemHorizontalPadding / 2),
            ) { label() }
        }
    }) { measurables, constraints ->
        val iconPlaceable =
            measurables.first { it.layoutId == IconLayoutIdTag }.measure(constraints)

        val labelPlaceable =
            label?.let {
                measurables.first { it.layoutId == LabelLayoutIdTag }.measure(
                    constraints.copy(minHeight = 0),
                )
            }

        if (label == null) {
            placeIcon(iconPlaceable, constraints)
        } else {
            placeLabelAndIcon(
                labelPlaceable!!,
                iconPlaceable,
                constraints,
                alwaysShowLabel,
                animationProgress,
            )
        }
    }
}

private fun MeasureScope.placeIcon(
    iconPlaceable: Placeable,
    constraints: Constraints,
): MeasureResult {
    val width = constraints.maxWidth
    val height = constraints.maxHeight

    val iconX = (width - iconPlaceable.width) / 2
    val iconY = (height - iconPlaceable.height) / 2

    return layout(width, height) {
        iconPlaceable.placeRelative(iconX, iconY)
    }
}

private fun MeasureScope.placeLabelAndIcon(
    labelPlaceable: Placeable,
    iconPlaceable: Placeable,
    constraints: Constraints,
    alwaysShowLabel: Boolean,
    animationProgress: Float,
): MeasureResult {
    val height = constraints.maxHeight

    val labelY =
        height - labelPlaceable.height

    val selectedIconY = NavigationBarItemVerticalPadding.roundToPx()
    val unselectedIconY =
        if (alwaysShowLabel) selectedIconY else (height - iconPlaceable.height) / 2

    val iconDistance = unselectedIconY - selectedIconY

    val offset = (iconDistance * (1 - animationProgress)).roundToInt()

    val containerWidth = constraints.maxWidth

    val labelX = (containerWidth - labelPlaceable.width) / 2
    val iconX = (containerWidth - iconPlaceable.width) / 2

    return layout(containerWidth, height) {
        if (alwaysShowLabel || animationProgress != 0f) {
            labelPlaceable.placeRelative(labelX, labelY + offset)
        }
        iconPlaceable.placeRelative(iconX, selectedIconY + offset)
    }
}

internal object NavigationBarDefaults {
    internal val NavigationBarHeight: Dp = 58.0.dp
    val containerColor: Color @Composable get() = BringAppTheme.colors.background

    val windowInsets: WindowInsets
        @Composable get() =
            WindowInsets.systemBars.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
            )
}

object NavigationBarItemDefaults {
    internal val NavigationBarItemHorizontalPadding: Dp = 8.dp
    internal val NavigationBarItemVerticalPadding: Dp = 8.dp
    internal const val ItemAnimationDurationMillis: Int = 100

    @Composable
    fun colors(
        selectedIconColor: Color = BringAppTheme.colors.onBackground,
        selectedTextColor: Color = BringAppTheme.colors.onBackground,
        unselectedIconColor: Color = BringAppTheme.colors.onBackground.copy(alpha = 0.65f),
        unselectedTextColor: Color = BringAppTheme.colors.onBackground.copy(alpha = 0.65f),
        disabledIconColor: Color = BringAppTheme.colors.onBackground.copy(alpha = 0.3f),
        disabledTextColor: Color = BringAppTheme.colors.onBackground.copy(alpha = 0.3f),
    ): NavigationBarItemColors =
        NavigationBarItemColors(
            selectedIconColor = selectedIconColor,
            selectedTextColor = selectedTextColor,
            unselectedIconColor = unselectedIconColor,
            unselectedTextColor = unselectedTextColor,
            disabledIconColor = disabledIconColor,
            disabledTextColor = disabledTextColor,
        )

    @Composable
    fun textStyle(): TextStyle = BringAppTheme.typography.label2
}

@ConsistentCopyVisibility
@Stable
data class NavigationBarItemColors internal constructor(
    private val selectedIconColor: Color,
    private val selectedTextColor: Color,
    private val unselectedIconColor: Color,
    private val unselectedTextColor: Color,
    private val disabledIconColor: Color,
    private val disabledTextColor: Color,
) {
    @Composable
    internal fun iconColor(selected: Boolean, enabled: Boolean): State<Color> {
        val targetValue =
            when {
                !enabled -> disabledIconColor
                selected -> selectedIconColor
                else -> unselectedIconColor
            }
        return animateColorAsState(
            targetValue = targetValue,
            animationSpec = tween(NavigationBarItemDefaults.ItemAnimationDurationMillis),
            label = "icon-color",
        )
    }

    @Composable
    internal fun textColor(selected: Boolean, enabled: Boolean): State<Color> {
        val targetValue =
            when {
                !enabled -> disabledTextColor
                selected -> selectedTextColor
                else -> unselectedTextColor
            }
        return animateColorAsState(
            targetValue = targetValue,
            animationSpec = tween(NavigationBarItemDefaults.ItemAnimationDurationMillis),
            label = "text-color",
        )
    }
}

private const val IconLayoutIdTag: String = "icon"
private const val LabelLayoutIdTag: String = "label"
