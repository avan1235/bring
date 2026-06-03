package `in`.procyk.bring

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.fetch.RequestInit

@OptIn(ExperimentalWasmJsInterop::class)
actual suspend fun loadRes(url: String): ByteArray =
    window.fetch(url, js("{}")).await().arrayBuffer().await().toByteArray()

private fun ArrayBuffer.toByteArray(): ByteArray {
    val source = Int8Array(this, 0, byteLength)
    return ByteArray(source.length) { source[it] }
}