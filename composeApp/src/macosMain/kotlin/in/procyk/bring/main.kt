package `in`.procyk.bring

import androidx.compose.ui.window.Window
import platform.AppKit.NSApplication

fun main() {
    val app = NSApplication.sharedApplication()
    Window("Bring!") {
        BringApp()
    }
    app.run()
}
