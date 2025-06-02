package `in`.procyk.bring.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
internal inline fun AppScreen(
    testTag: String,
    crossinline content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag(testTag),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}