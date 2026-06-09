package `in`.procyk.bring.ui.components

import androidx.annotation.IntRange
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.nomanr.composables.slider.*
import `in`.procyk.bring.ui.BringAppTheme

@Composable
internal fun Slider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    @IntRange(from = 0) steps: Int = 0,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
) {
    val state =
        remember(steps, valueRange) {
            SliderState(
                value,
                steps,
                onValueChangeFinished,
                valueRange,
            )
        }

    state.onValueChangeFinished = onValueChangeFinished
    state.onValueChange = onValueChange
    state.value = value

    Slider(
        state = state,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        colors = colors,
    )
}

@Composable
internal fun Slider(
    state: SliderState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    require(state.steps >= 0) { "steps should be >= 0" }

    BasicSlider(
        modifier = modifier,
        state = state,
        colors = colors,
        enabled = enabled,
        interactionSource = interactionSource,
    )
}

@Composable
internal fun RangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange(from = 0) steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    startInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    endInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val state =
        remember(steps, valueRange) {
            RangeSliderState(
                value.start,
                value.endInclusive,
                steps,
                onValueChangeFinished,
                valueRange,
            )
        }

    state.onValueChangeFinished = onValueChangeFinished
    state.onValueChange = { onValueChange(it.start..it.endInclusive) }
    state.activeRangeStart = value.start
    state.activeRangeEnd = value.endInclusive

    RangeSlider(
        state = state,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        startInteractionSource = startInteractionSource,
        endInteractionSource = endInteractionSource,
    )
}

@Composable
internal fun RangeSlider(
    state: RangeSliderState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SliderColors = SliderDefaults.colors(),
    startInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    endInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    require(state.steps >= 0) { "steps should be >= 0" }

    BasicRangeSlider(
        modifier = modifier,
        state = state,
        enabled = enabled,
        startInteractionSource = startInteractionSource,
        endInteractionSource = endInteractionSource,
        colors = colors,
    )
}

@Stable
object SliderDefaults {
    @Composable
    fun colors(
        thumbColor: Color = BringAppTheme.colors.primary,
        activeTrackColor: Color = BringAppTheme.colors.primary,
        activeTickColor: Color = BringAppTheme.colors.onPrimary,
        inactiveTrackColor: Color = BringAppTheme.colors.secondary,
        inactiveTickColor: Color = BringAppTheme.colors.primary,
        disabledThumbColor: Color = BringAppTheme.colors.disabled,
        disabledActiveTrackColor: Color = BringAppTheme.colors.disabled,
        disabledActiveTickColor: Color = BringAppTheme.colors.disabled,
        disabledInactiveTrackColor: Color = BringAppTheme.colors.disabled,
        disabledInactiveTickColor: Color = Color.Unspecified,
    ) = SliderColors(
        thumbColor = thumbColor,
        activeTrackColor = activeTrackColor,
        activeTickColor = activeTickColor,
        inactiveTrackColor = inactiveTrackColor,
        inactiveTickColor = inactiveTickColor,
        disabledThumbColor = disabledThumbColor,
        disabledActiveTrackColor = disabledActiveTrackColor,
        disabledActiveTickColor = disabledActiveTickColor,
        disabledInactiveTrackColor = disabledInactiveTrackColor,
        disabledInactiveTickColor = disabledInactiveTickColor,
    )
}
