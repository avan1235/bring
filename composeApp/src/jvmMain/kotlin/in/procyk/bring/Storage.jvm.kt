package `in`.procyk.bring

import androidx.compose.runtime.Composable
import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.file.FileCodec
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.Serializable
import net.harawata.appdirs.AppDirsFactory
import kotlin.uuid.Uuid

@Composable
internal actual inline fun <reified T : @Serializable Any> bringCodec(): Codec<T> {
    val filesDir = AppDirsFactory.getInstance().getUserDataDir(
        ComposeAppConfig.PACKAGE,
        ComposeAppConfig.VERSION,
        ComposeAppConfig.AUTHOR,
    )
    val file = Path(filesDir)

    with(SystemFileSystem) { if (!exists(file)) createDirectories(file) }

    return FileCodec<T>(Path(file, ".bring"), tempFile = Path(file, Uuid.random().toHexDashString()))
}
