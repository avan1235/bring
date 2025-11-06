@file:OptIn(ExperimentalComposeUiApi::class)

package `in`.procyk.bring

import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.window.ComposeViewport
import com.materialkolor.ktx.toHex
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.vm.PlatformContext
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.asList

fun main() {
    val head = document.head ?: error("no <head>")
    val platformContext = PlatformContext()

    ComposeViewport {
        val fontFamilyResolver = LocalFontFamilyResolver.current
        var fontsLoaded by remember { mutableStateOf(false) }

        when {
            fontsLoaded -> {
                BringAppTheme(platformContext) { context ->
                    val backgroundColor = BringAppTheme.colors.background
                    LaunchedEffect(backgroundColor) {
                        head.children.asList().single { it.getAttribute("name") == "theme-color" }.remove()
                        document.createElement("meta").apply {
                            setAttribute("name", "theme-color")
                            setAttribute("content", backgroundColor.toHex())
                        }.let(head::appendChild)
                    }
                    BringAppInternal(
                        context = context,
                        initListId = when {
                            window.location.hash.isNotEmpty() -> window.location.hash.substring(1)
                            else -> null
                        }
                    )
                }
            }

            else -> {}
        }

        LaunchedEffect(Unit) {
            val notoColorEmojiBytes = loadRes(NotoColorEmoji)
            val fontFamily = FontFamily(listOf(Font("NotoColorEmoji", notoColorEmojiBytes)))
            fontFamilyResolver.preload(fontFamily)
            fontsLoaded = true
        }
    }
}

private const val NotoColorEmoji: String = "./NotoColorEmoji.ttf"

internal expect suspend fun loadRes(url: String): ByteArray