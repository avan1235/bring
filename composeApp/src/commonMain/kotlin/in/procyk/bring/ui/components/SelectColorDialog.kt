package `in`.procyk.bring.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import bring.composeapp.generated.resources.*
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SelectColorDialog(
    selectedColor: Color,
    previousColor: MutableState<Color?>,
    onColorReset: (() -> Unit)? = null,
    onColorSaved: (Color) -> Unit,
) {
    AnimatedVisibility(previousColor.value != null) {
        val controller = rememberColorPickerController()
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
                ) {
                    HsvColorPicker(
                        modifier = Modifier.fillMaxWidth().height(240.dp).padding(10.dp),
                        controller = controller,
                        initialColor = selectedColor,
                        onColorChanged = { onColorSaved(it.color) }
                    )
                }
            },
        )
    }
}
