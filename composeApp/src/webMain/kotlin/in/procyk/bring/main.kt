@file:OptIn(ExperimentalComposeUiApi::class)

package `in`.procyk.bring

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.window.ComposeViewport
import com.materialkolor.ktx.toHex
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.ui.Theme
import `in`.procyk.bring.ui.Theme.*
import `in`.procyk.bring.vm.PlatformContext
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.flow.mapNotNull
import org.w3c.dom.Element
import org.w3c.dom.HTMLHeadElement
import org.w3c.dom.HTMLMetaElement
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
                    val themeColorDarkMode by context.store.updates
                        .mapNotNull { it?.run { themeColor to darkMode } }
                        .collectAsState(Color.White.toArgb() to System)
                    val isSystemInDarkTheme = isSystemInDarkTheme()
                    LaunchedEffect(themeColorDarkMode) {
                        val color = Color(themeColorDarkMode.first)
                        sequenceOf(
                            head.replaceThemeColor(color),
                            head.replaceColorScheme(themeColorDarkMode.second, isSystemInDarkTheme),
                            head.replaceLastStyle(color),
                        ).forEach(head::appendChild)
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

private fun HTMLHeadElement.replaceThemeColor(color: Color): Element {
    children.asList().single { it is HTMLMetaElement && it.getAttribute("name") == "theme-color" }.remove()
    return document.createElement("meta").apply {
        setAttribute("name", "theme-color")
        setAttribute("content", color.toHex())
    }
}

private fun HTMLHeadElement.replaceColorScheme(theme: Theme, isSystemInDarkTheme: Boolean): Element {
    children.asList().single { it is HTMLMetaElement && it.getAttribute("name") == "color-scheme" }.remove()
    return document.createElement("meta").apply {
        setAttribute("name", "color-scheme")
        setAttribute(
            "content",
            when (theme) {
                System -> if (isSystemInDarkTheme) "only dark" else "only light"
                Dark -> "only dark"
                Light -> "only light"
            }
        )
    }
}

private fun HTMLHeadElement.replaceLastStyle(color: Color): Element {
    children.asList().last { it.localName == "style" }.remove()
    return document.createElement("style").apply {
        innerHTML = "html, body { background-color: ${color.toHex()} !important; }"
    }
}

private const val NotoColorEmoji: String = "./NotoColorEmoji.ttf"

internal expect suspend fun loadRes(url: String): ByteArray