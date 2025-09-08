package `in`.procyk.bring

import androidx.compose.runtime.Composable
import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.file.FileCodec
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import kotlin.uuid.Uuid

@OptIn(ExperimentalForeignApi::class)
@Composable
actual inline fun <reified T : @Serializable Any> bringCodec(): Codec<T> {
    val fileManager = NSFileManager.defaultManager
    val documentsUrl = fileManager.URLForDirectory(
        directory = NSDocumentDirectory,
        appropriateForURL = null,
        create = false,
        inDomain = NSUserDomainMask,
        error = null
    )!!

    val documentsPath = documentsUrl.path!!
    return FileCodec<T>(
        file = Path(documentsPath, ".bring"),
        tempFile = Path(documentsPath, Uuid.random().toHexDashString())
    )
}
