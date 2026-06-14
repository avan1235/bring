package `in`.procyk.bring

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import `in`.procyk.bring.vm.PlatformContext
import io.github.vinceglb.filekit.FileKit

fun main() {
    FileKit.init(appId = AppConfig.PACKAGE)

    val platformContext = PlatformContext()

    singleWindowApplication(
        title = AppConfig.APP_NAME,
        state = WindowState(width = 600.dp, height = 800.dp),
    ) {
        BringApp(platformContext)
    }
}
