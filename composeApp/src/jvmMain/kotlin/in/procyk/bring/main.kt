package `in`.procyk.bring

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication

fun main() = singleWindowApplication(
    title = ComposeAppConfig.APP_NAME,
    state = WindowState(width = 600.dp, height = 800.dp),
) {
    BringApp()
}
