@file:OptIn(ExperimentalComposeUiApi::class)

package `in`.procyk.bring

import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.window.ComposeViewport
import com.materialkolor.ktx.toHex
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.vm.PlatformContext
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLHeadElement
import org.w3c.dom.HTMLMetaElement
import org.w3c.dom.asList

fun main() {
    val head = document.head ?: error("no <head>")
    val body = document.body ?: error("no <body>")
    val platformContext = PlatformContext()

    ComposeViewport {
        val fontFamilyResolver = LocalFontFamilyResolver.current
        var fontsLoaded by remember { mutableStateOf(false) }

        when {
            fontsLoaded -> {
                BringAppTheme(platformContext) { context ->
                    val backgroundColor = BringAppTheme.colors.background
                    LaunchedEffect(backgroundColor) {
                        head.replaceThemeColor(backgroundColor)
                        body.replaceBackgroundColor(backgroundColor)
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

private fun HTMLHeadElement.replaceThemeColor(color: Color) {
    children.asList()
        .filterIsInstance<HTMLMetaElement>()
        .filter { it.getAttribute("name") == "theme-color" }
        .forEach { it.remove() }

    fun addThemeColor(content: String, media: String? = null) {
        val node = document.createElement("meta").apply {
            setAttribute("name", "theme-color")
            setAttribute("content", content)
            if (media != null) setAttribute("media", media)
        }
        appendChild(node)
    }

    val hex = color.toHex()
    addThemeColor(hex)
    addThemeColor(hex, "(prefers-color-scheme: light)")
    addThemeColor(hex, "(prefers-color-scheme: dark)")
}

private fun HTMLElement.replaceBackgroundColor(color: Color) {
    setAttribute("style", "background-color: rgb(${color.red.in255()}, ${color.green.in255()}, ${color.blue.in255()});")
}

private fun Float.in255(): Int = (this * 255).toInt()

private const val NotoColorEmoji: String = "./NotoColorEmoji.ttf"

internal expect suspend fun loadRes(url: String): ByteArray