package `in`.procyk.bring.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.ui.components.textfield.TextField

@Composable
internal fun RowScope.AddItemTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onAdd: () -> Unit,
    onDone: () -> Unit,
    loading: Boolean = false,
    shape: Shape = RoundedCornerShape(size = 8.dp),
    borderWidth: Dp = 2.dp,
    animationDuration: Int = 4000,
    animationColor: Color = BringAppTheme.colors.primary,
    backgroundAnimationColor: Color = BringAppTheme.colors.background,
    textFieldModifier: Modifier = Modifier,
    buttonEnabled: Boolean = true,
    buttonModifier: Modifier = Modifier,
) {
    when {
        loading -> {
            val infiniteTransition = rememberInfiniteTransition()
            val progress by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = animationDuration, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
            )
            InputTextField(
                value = value,
                onValueChange = onValueChange,
                onDone = onDone,
                shape = shape,
                borderWidth = borderWidth * (0.5f + progress),
                textFieldModifier = textFieldModifier,
                brush = when {
                    progress < 0.5f -> {
                        val normalizedProgress = progress / 0.5f
                        Brush.linearGradient(
                            0f to lerp(backgroundAnimationColor, animationColor, 1f - normalizedProgress),
                            progress to animationColor,
                            progress + 0.5f to backgroundAnimationColor,
                            1f to lerp(backgroundAnimationColor, animationColor, normalizedProgress),
                        )
                    }

                    else -> {
                        val normalizedProgress = (progress - 0.5f) / 0.5f
                        Brush.linearGradient(
                            0f to lerp(animationColor, backgroundAnimationColor, 1f - normalizedProgress),
                            progress - 0.5f to backgroundAnimationColor,
                            progress to animationColor,
                            1f to lerp(animationColor, backgroundAnimationColor, normalizedProgress),
                        )
                    }
                }
            )
        }

        else -> InputTextField(
            value = value,
            onValueChange = onValueChange,
            onDone = onDone,
            shape = shape,
            borderWidth = borderWidth,
            textFieldModifier = textFieldModifier
        )
    }


    IconButton(
        onClick = { onAdd() },
        variant = IconButtonVariant.Primary,
        enabled = buttonEnabled,
        modifier = buttonModifier,
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
        )
    }
}

@Composable
private fun RowScope.InputTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    shape: Shape,
    borderWidth: Dp,
    textFieldModifier: Modifier,
    brush: Brush = SolidColor(BringAppTheme.colors.primary),
) {
    TextField(
        value = value,
        onValueChange = { onValueChange(it) },
        modifier = textFieldModifier
            .drawWithCache {
                val outline = shape.createOutline(size, layoutDirection, this)
                onDrawBehind {
                    drawOutline(
                        outline = outline,
                        brush = brush,
                        style = Stroke(width = 2 * borderWidth.toPx())
                    )
                }
            }
            .weight(1f, fill = false),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            autoCorrectEnabled = false,
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Text,
            showKeyboardOnFocus = true,
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone() },
        )
    )
}
