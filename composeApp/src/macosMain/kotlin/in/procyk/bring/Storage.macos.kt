package `in`.procyk.bring

import androidx.compose.runtime.Composable
import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.file.FileCodec
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.Serializable
import platform.Foundation.NSHomeDirectory

@Composable
actual inline fun <reified T : @Serializable Any> bringCodec(): Codec<T> {
    val file = Path(NSHomeDirectory())

    with(SystemFileSystem) { if (!exists(file)) createDirectories(file) }

    return FileCodec<T>(Path(file, ".bring"))
}
