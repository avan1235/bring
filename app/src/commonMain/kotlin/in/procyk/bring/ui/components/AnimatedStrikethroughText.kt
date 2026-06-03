package `in`.procyk.bring.ui.components

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import `in`.procyk.bring.ui.LocalTextStyle

@Composable
internal fun AnimatedStrikethroughText(
    text: String,
    modifier: Modifier = Modifier,
    isChecked: Boolean = true,
    animateOnHide: Boolean = true,
    spec: AnimationSpec<Int> = tween(500, easing = FastOutLinearInEasing),
    strikethroughStyle: SpanStyle = SpanStyle(),
    textStyle: TextStyle = LocalTextStyle.current
) {
    var textToDisplay by remember { mutableStateOf(AnnotatedString("")) }

    val length = remember { Animatable(initialValue = 0, typeConverter = Int.VectorConverter) }

    val graphemeClusters = remember(text) { text.getGraphemeClusterBoundaries() }

    LaunchedEffect(length.value) {
        val adjustedLength = findNearestClusterBoundary(length.value, graphemeClusters)
        textToDisplay = text.buildStrikethrough(adjustedLength, strikethroughStyle)
    }

    LaunchedEffect(isChecked) {
        when {
            isChecked -> length.animateTo(text.length, spec)
            !isChecked && animateOnHide -> length.animateTo(0, spec)
            else -> length.snapTo(0)
        }
    }

    LaunchedEffect(text) {
        when {
            isChecked && text.length == length.value -> {
                val adjustedLength = findNearestClusterBoundary(length.value, graphemeClusters)
                textToDisplay = text.buildStrikethrough(adjustedLength, strikethroughStyle)
            }

            isChecked && text.length != length.value -> length.snapTo(text.length)
            else -> textToDisplay = AnnotatedString(text)
        }
    }

    Text(
        text = textToDisplay,
        modifier = modifier,
        style = textStyle,
        maxLines = 1,
        overflow = TextOverflow.MiddleEllipsis,
    )
}

private fun String.buildStrikethrough(length: Int, style: SpanStyle) = buildAnnotatedString {
    append(this@buildStrikethrough)
    val lineThroughStyle = style.copy(textDecoration = TextDecoration.LineThrough)
    if (length > 0) {
        addStyle(lineThroughStyle, 0, length)
    }
}

private fun String.getGraphemeClusterBoundaries(): List<Int> {
    val boundaries = mutableListOf<Int>()
    var i = 0
    while (i < this.length) {
        val next = i + when {
            this[i].isHighSurrogate() -> 2
            i < this.length - 1 && this[i + 1].isCombining() -> 2
            else -> 1
        }
        boundaries.add(next)
        i = next
    }
    return boundaries
}

private fun findNearestClusterBoundary(target: Int, boundaries: List<Int>): Int {
    return boundaries.lastOrNull { it <= target } ?: 0
}

private fun Char.isHighSurrogate(): Boolean = this.code in 0xD800..0xDBFF
private fun Char.isCombining(): Boolean = this.code in 0x0300..0x036F ||
        this.code in 0x1AB0..0x1AFF ||
        this.code in 0x20D0..0x20FF
