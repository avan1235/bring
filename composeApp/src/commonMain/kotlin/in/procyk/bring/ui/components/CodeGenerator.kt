package `in`.procyk.bring.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.times
import `in`.procyk.bring.Code

private const val OVERLAP_PIXELS = 0.5f

@Composable
internal fun CodeGenerator(
    code: Code,
    color: Color,
    width: Dp,
    modifier: Modifier = Modifier
) {
    val bits = code.bits
    val is1D = bits.height == 1

    Canvas(
        modifier = modifier.requiredSize(
            width = width,
            height = when {
                is1D -> width / 2
                else -> bits.height.toDouble() / bits.width.toDouble() * width
            },
        )
    ) {
        val canvasWidth = this.size.width
        val canvasHeight = this.size.height

        val codeWidth = bits.width.toFloat()
        val codeHeight = bits.height.toFloat()

        val scale = minOf(canvasWidth / codeWidth, canvasHeight / codeHeight)

        val leftPadding = maxOf(0f, (canvasWidth - (codeWidth * scale)) / 2f)
        val topPadding = if (is1D) 0f else maxOf(0f, (canvasHeight - (codeHeight * scale)) / 2f)

        for (moduleY in 0..<bits.height) {
            var startX = -1
            for (moduleX in 0..bits.width) {
                val isFilled = moduleX < bits.width && code[moduleX, moduleY]
                when {
                    isFilled && startX == -1 -> startX = moduleX
                    !isFilled && startX != -1 -> {
                        val span = moduleX - startX
                        drawRect(
                            color = color,
                            topLeft = Offset(
                                x = leftPadding + (startX * scale),
                                y = topPadding + (moduleY * scale),
                            ),
                            size = Size(
                                width = (span * scale) + OVERLAP_PIXELS,
                                height = if (is1D) canvasHeight else scale + OVERLAP_PIXELS,
                            )
                        )
                        startX = -1
                    }
                }
            }
        }
    }
}