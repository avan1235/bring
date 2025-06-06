package `in`.procyk.bring

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.ui.components.Surface
import `in`.procyk.bring.ui.components.progressindicators.CircularProgressIndicator
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.dom.asList
import org.w3c.fetch.Response
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.body ?: return

    ComposeViewport(body) {
        val fontFamilyResolver = LocalFontFamilyResolver.current
        var fontsLoaded by remember { mutableStateOf(false) }

        when {
            fontsLoaded -> BringApp(
                initListId = when {
                    window.location.hash.isNotEmpty() -> window.location.hash.substring(1)
                    else -> null
                }
            )

            else -> {}
        }

        LaunchedEffect(Unit) {
            val notoColorEmojiBytes = loadRes(NotoColorEmoji).toByteArray()
            val fontFamily = FontFamily(listOf(Font("NotoColorEmoji", notoColorEmojiBytes)))
            fontFamilyResolver.preload(fontFamily)
            body.children.asList().forEach { it.remove() }
            fontsLoaded = true
        }
    }
}

private const val NotoColorEmoji: String = "./NotoColorEmoji.ttf"

private suspend fun loadRes(url: String): ArrayBuffer =
    window.fetch(url).await<Response>().arrayBuffer().await()

private fun ArrayBuffer.toByteArray(): ByteArray {
    val source = Int8Array(this, 0, byteLength)
    return jsInt8ArrayToKotlinByteArray(source)
}

@JsFun(
    """ (src, size, dstAddr) => {
        const mem8 = new Int8Array(wasmExports.memory.buffer, dstAddr, size);
        mem8.set(src);
    }
"""
)
private external fun jsExportInt8ArrayToWasm(src: Int8Array, size: Int, dstAddr: Int)

private fun jsInt8ArrayToKotlinByteArray(x: Int8Array): ByteArray {
    val size = x.length

    @OptIn(UnsafeWasmMemoryApi::class)
    return withScopedMemoryAllocator { allocator ->
        val memBuffer = allocator.allocate(size)
        val dstAddress = memBuffer.address.toInt()
        jsExportInt8ArrayToWasm(x, size, dstAddress)
        ByteArray(size) { i -> (memBuffer + i).loadByte() }
    }
}
