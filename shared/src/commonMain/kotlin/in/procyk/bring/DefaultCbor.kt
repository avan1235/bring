package `in`.procyk.bring

import arrow.core.serialization.ArrowModule
import kotlinx.serialization.cbor.Cbor

val DefaultCbor = Cbor {
    encodeDefaults = true
    ignoreUnknownKeys = true
    preferCborLabelsOverNames = true
    alwaysUseByteString = true
    serializersModule = ArrowModule
}
