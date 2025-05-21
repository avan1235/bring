package `in`.procyk.bring

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.file.FileCodec
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable

@Composable
actual inline fun <reified T : @Serializable Any> bringCodec(): Codec<T> {
    val context = LocalContext.current
    val file = context.filesDir.resolve(".bring")

    return FileCodec<T>(file = Path(file.absolutePath))
}
