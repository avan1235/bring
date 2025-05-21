package `in`.procyk.bring

import androidx.compose.runtime.Composable
import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.storage.StorageCodec
import kotlinx.serialization.Serializable

@Composable
actual inline fun <reified T : @Serializable Any> bringCodec(): Codec<T> =
    StorageCodec(".bring")
