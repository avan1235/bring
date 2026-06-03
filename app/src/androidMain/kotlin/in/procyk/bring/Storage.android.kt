package `in`.procyk.bring

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.file.FileCodec
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import java.io.File
import kotlin.uuid.Uuid

@Composable
internal actual inline fun <reified T : @Serializable Any> bringCodec(): Codec<T> {
    val context = LocalContext.current
    val filesDir = context.filesDir
    val file = filesDir.resolve(".bring")

    return FileCodec<T>(
        file = Path(file.absolutePath),
        tempFile = Path(filesDir.resolve(Uuid.random().toHexDashString()).absolutePath)
    )
}
