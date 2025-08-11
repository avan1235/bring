package `in`.procyk.bring.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag

@Composable
internal inline fun AppScreen(
    testTag: String,
    padding: PaddingValues,
    crossinline content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag(testTag),
    ) {
        val layoutDirection = LocalLayoutDirection.current
        Box(
            modifier = Modifier
                .padding(
                    start = padding.calculateStartPadding(layoutDirection),
                    end = padding.calculateEndPadding(layoutDirection)
                )
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}