package `in`.procyk.bring.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import bring.composeapp.generated.resources.*
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.materialkolor.ktx.toHex
import `in`.procyk.bring.ui.BringAppTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SelectColorDialog(
    selectedColor: Color,
    previousColor: MutableState<Color?>,
    onColorReset: (() -> Unit)? = null,
    showBrightnessSlider: Boolean = true,
    onColorSaved: (Color) -> Unit,
) {
    val selectedColor = if (selectedColor == Color.Unspecified) BringAppTheme.colors.primary else selectedColor
    AnimatedVisibility(previousColor.value != null) {
        val density = LocalDensity.current
        val controller = rememberColorPickerController()
        LaunchedEffect(controller) {
            controller.wheelPaint = density.wheelPaint()
        }
        fun onDismissRequest() {
            previousColor.value?.let(onColorSaved)
            previousColor.value = null
        }
        AlertDialogComponent(
            onDismissRequest = ::onDismissRequest,
            confirmButton = {
                Button(
                    variant = ButtonVariant.PrimaryOutlined, text = stringResource(Res.string.save),
                    onClick = {
                        previousColor.value = null
                    })
            },
            dismissButton = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    onColorReset?.let { onColorReset ->
                        Button(
                            variant = ButtonVariant.Ghost,
                            text = stringResource(Res.string.clear),
                            onClick = {
                                onColorReset()
                                previousColor.value = null
                            }
                        )
                    }
                    Button(
                        variant = ButtonVariant.Ghost,
                        text = stringResource(Res.string.cancel),
                        onClick = ::onDismissRequest
                    )
                }
            },
            title = { Text(text = stringResource(Res.string.select_color)) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var currentColor by remember { mutableStateOf(selectedColor) }
                        Column(
                            modifier = Modifier.width(120.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .border(1.dp, BringAppTheme.colors.primary, RoundedCornerShape(8.dp))
                                    .background(currentColor, RoundedCornerShape(8.dp))
                                    .size(64.dp)
                            )
                            Text(text = currentColor.toHex())
                        }
                        HsvColorPicker(
                            modifier = Modifier.size(240.dp).padding(10.dp),
                            controller = controller,
                            initialColor = selectedColor,
                            onColorChanged = {
                                currentColor = it.color
                                onColorSaved(it.color)
                            }
                        )
                    }
                    if (showBrightnessSlider) BrightnessSlider(
                        modifier = Modifier.size(width = 380.dp, height = 32.dp),
                        controller = controller,
                        initialColor = selectedColor,
                        wheelPaint = density.wheelPaint(),
                    )
                }
            },
        )
    }
}

private fun Density.wheelPaint(): Paint = Paint().apply {
    color = Color.White
    alpha = 1.0f
    style = PaintingStyle.Stroke
    strokeWidth = with(this@wheelPaint) { 2.dp.toPx() }
}
