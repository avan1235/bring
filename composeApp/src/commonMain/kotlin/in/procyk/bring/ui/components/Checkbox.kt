package `in`.procyk.bring.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.ui.components.CheckboxDefaults.BoxInDuration
import `in`.procyk.bring.ui.components.CheckboxDefaults.BoxOutDuration
import `in`.procyk.bring.ui.components.CheckboxDefaults.CheckAnimationDuration
import `in`.procyk.bring.ui.components.CheckboxDefaults.CheckboxDefaultPadding
import `in`.procyk.bring.ui.components.CheckboxDefaults.CheckboxSize
import `in`.procyk.bring.ui.components.CheckboxDefaults.MinimumInteractiveSize
import `in`.procyk.bring.ui.components.CheckboxDefaults.RadiusSize
import `in`.procyk.bring.ui.components.CheckboxDefaults.StrokeWidth
import `in`.procyk.bring.ui.foundation.ripple
import kotlin.math.floor
import kotlin.math.max

@Composable
internal fun Checkbox(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    enabled: Boolean = true,
    colors: CheckboxColors = CheckboxDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    TriStateCheckbox(
        state = ToggleableState(checked),
        onClick =
            if (onCheckedChange != null) {
                { onCheckedChange(!checked) }
            } else {
                null
            },
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
    )
}

@Composable
internal fun TriStateCheckbox(
    state: ToggleableState,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CheckboxColors = CheckboxDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val toggleableModifier =
        if (onClick != null) {
            Modifier
                .requiredSize(MinimumInteractiveSize)
                .triStateToggleable(
                    state = state,
                    onClick = onClick,
                    enabled = enabled,
                    role = Role.Checkbox,
                    interactionSource = interactionSource,
                    indication =
                        ripple(
                            bounded = false,
                            radius = MinimumInteractiveSize / 2,
                        ),
                )
        } else {
            Modifier
        }
    CheckboxComponent(
        enabled = enabled,
        value = state,
        modifier =
            modifier
                .then(toggleableModifier)
                .padding(CheckboxDefaultPadding),
        colors = colors,
    )
}

@Composable
private fun CheckboxComponent(
    enabled: Boolean,
    value: ToggleableState,
    modifier: Modifier,
    colors: CheckboxColors,
) {
    val transition = updateTransition(value, label = "checkbox")
    val checkDrawFraction =
        transition.animateFloat(
            transitionSpec = {
                when {
                    initialState == ToggleableState.Off -> snap()
                    targetState == ToggleableState.Off -> snap(delayMillis = BoxOutDuration)
                    else -> tween(durationMillis = CheckAnimationDuration)
                }
            },
            label = "checkDrawFraction",
        ) {
            when (it) {
                ToggleableState.On -> 1f
                ToggleableState.Off -> 0f
                ToggleableState.Indeterminate -> 1f
            }
        }

    val checkCenterGravitationShiftFraction =
        transition.animateFloat(
            transitionSpec = {
                when {
                    initialState == ToggleableState.Off -> snap()
                    targetState == ToggleableState.Off -> snap(delayMillis = BoxOutDuration)
                    else -> tween(durationMillis = CheckAnimationDuration)
                }
            },
            label = "checkCenterGravitationShiftFraction",
        ) {
            when (it) {
                ToggleableState.On -> 0f
                ToggleableState.Off -> 0f
                ToggleableState.Indeterminate -> 1f
            }
        }
    val checkCache = remember { CheckDrawingCache() }
    val checkColor = colors.checkmarkColor(value)
    val boxColor = colors.boxColor(enabled, value)
    val borderColor = colors.borderColor(enabled, value)

    Canvas(modifier.wrapContentSize(Alignment.Center).requiredSize(CheckboxSize)) {
        val strokeWidthPx = floor(StrokeWidth.toPx())
        drawBox(
            boxColor = boxColor.value,
            borderColor = borderColor.value,
            radius = RadiusSize.toPx(),
            strokeWidth = strokeWidthPx,
        )
        drawCheck(
            checkColor = checkColor,
            checkFraction = checkDrawFraction.value,
            crossCenterGravitation = checkCenterGravitationShiftFraction.value,
            strokeWidthPx = strokeWidthPx,
            drawingCache = checkCache,
        )
    }
}

private fun DrawScope.drawBox(
    boxColor: Color,
    borderColor: Color,
    radius: Float,
    strokeWidth: Float,
) {
    val halfStrokeWidth = strokeWidth / 2.0f
    val stroke = Stroke(strokeWidth)
    val checkboxSize = size.width
    if (boxColor == borderColor) {
        drawRoundRect(
            boxColor,
            size = Size(checkboxSize, checkboxSize),
            cornerRadius = CornerRadius(radius),
            style = Fill,
        )
    } else {
        drawRoundRect(
            boxColor,
            topLeft = Offset(strokeWidth, strokeWidth),
            size = Size(checkboxSize - strokeWidth * 2, checkboxSize - strokeWidth * 2),
            cornerRadius = CornerRadius(max(0f, radius - strokeWidth)),
            style = Fill,
        )
        drawRoundRect(
            borderColor,
            topLeft = Offset(halfStrokeWidth, halfStrokeWidth),
            size = Size(checkboxSize - strokeWidth, checkboxSize - strokeWidth),
            cornerRadius = CornerRadius(radius - halfStrokeWidth),
            style = stroke,
        )
    }
}

private fun DrawScope.drawCheck(
    checkColor: Color,
    checkFraction: Float,
    crossCenterGravitation: Float,
    strokeWidthPx: Float,
    drawingCache: CheckDrawingCache,
) {
    val stroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Square)
    val width = size.width
    val checkCrossX = 0.4f
    val checkCrossY = 0.7f
    val leftX = 0.2f
    val leftY = 0.5f
    val rightX = 0.8f
    val rightY = 0.3f

    val gravitatedCrossX = lerp(checkCrossX, 0.5f, crossCenterGravitation)
    val gravitatedCrossY = lerp(checkCrossY, 0.5f, crossCenterGravitation)
    val gravitatedLeftY = lerp(leftY, 0.5f, crossCenterGravitation)
    val gravitatedRightY = lerp(rightY, 0.5f, crossCenterGravitation)

    with(drawingCache) {
        checkPath.reset()
        checkPath.moveTo(width * leftX, width * gravitatedLeftY)
        checkPath.lineTo(width * gravitatedCrossX, width * gravitatedCrossY)
        checkPath.lineTo(width * rightX, width * gravitatedRightY)
        pathMeasure.setPath(checkPath, false)
        pathToDraw.reset()
        pathMeasure.getSegment(
            0f,
            pathMeasure.length * checkFraction,
            pathToDraw,
            true,
        )
    }
    drawPath(drawingCache.pathToDraw, checkColor, style = stroke)
}

@Immutable
private class CheckDrawingCache(
    val checkPath: Path = Path(),
    val pathMeasure: PathMeasure = PathMeasure(),
    val pathToDraw: Path = Path(),
)

object CheckboxDefaults {
    internal const val BoxInDuration = 50
    internal const val BoxOutDuration = 100
    internal const val CheckAnimationDuration = 100

    internal val CheckboxDefaultPadding = 2.dp
    internal val CheckboxSize = 20.dp
    internal val StrokeWidth = 2.dp
    internal val RadiusSize = 4.dp
    internal val MinimumInteractiveSize = 36.dp

    @Composable
    fun colors() =
        CheckboxColors(
            checkedCheckmarkColor = BringAppTheme.colors.onPrimary,
            uncheckedCheckmarkColor = BringAppTheme.colors.transparent,
            checkedBoxColor = BringAppTheme.colors.primary,
            uncheckedBoxColor = BringAppTheme.colors.transparent,
            disabledCheckedBoxColor = BringAppTheme.colors.disabled,
            disabledUncheckedBoxColor = BringAppTheme.colors.transparent,
            disabledIndeterminateBoxColor = BringAppTheme.colors.disabled,
            checkedBorderColor = BringAppTheme.colors.primary,
            uncheckedBorderColor = BringAppTheme.colors.primary,
            disabledBorderColor = BringAppTheme.colors.disabled,
            disabledUncheckedBorderColor = BringAppTheme.colors.disabled,
            disabledIndeterminateBorderColor = BringAppTheme.colors.disabled,
        )
}

@Immutable
data class CheckboxColors(
    val checkedCheckmarkColor: Color,
    val uncheckedCheckmarkColor: Color,
    val checkedBoxColor: Color,
    val uncheckedBoxColor: Color,
    val disabledCheckedBoxColor: Color,
    val disabledUncheckedBoxColor: Color,
    val disabledIndeterminateBoxColor: Color,
    val checkedBorderColor: Color,
    val uncheckedBorderColor: Color,
    val disabledBorderColor: Color,
    val disabledUncheckedBorderColor: Color,
    val disabledIndeterminateBorderColor: Color,
) {
    @Composable
    internal fun checkmarkColor(state: ToggleableState): Color {
        return if (state == ToggleableState.Off) {
            uncheckedCheckmarkColor
        } else {
            checkedCheckmarkColor
        }
    }

    @Composable
    internal fun boxColor(enabled: Boolean, state: ToggleableState): State<Color> {
        val target =
            if (enabled) {
                when (state) {
                    ToggleableState.On, ToggleableState.Indeterminate -> checkedBoxColor
                    ToggleableState.Off -> uncheckedBoxColor
                }
            } else {
                when (state) {
                    ToggleableState.On -> disabledCheckedBoxColor
                    ToggleableState.Indeterminate -> disabledIndeterminateBoxColor
                    ToggleableState.Off -> disabledUncheckedBoxColor
                }
            }

        return if (enabled) {
            val duration = if (state == ToggleableState.Off) BoxOutDuration else BoxInDuration
            animateColorAsState(target, tween(durationMillis = duration), label = "animate box color")
        } else {
            rememberUpdatedState(target)
        }
    }

    @Composable
    internal fun borderColor(enabled: Boolean, state: ToggleableState): State<Color> {
        val target =
            if (enabled) {
                when (state) {
                    ToggleableState.On, ToggleableState.Indeterminate -> checkedBorderColor
                    ToggleableState.Off -> uncheckedBorderColor
                }
            } else {
                when (state) {
                    ToggleableState.Indeterminate -> disabledIndeterminateBorderColor
                    ToggleableState.On -> disabledBorderColor
                    ToggleableState.Off -> disabledUncheckedBorderColor
                }
            }

        return if (enabled) {
            val duration = if (state == ToggleableState.Off) BoxOutDuration else BoxInDuration
            animateColorAsState(target, tween(durationMillis = duration), label = "animate border color")
        } else {
            rememberUpdatedState(target)
        }
    }
}
