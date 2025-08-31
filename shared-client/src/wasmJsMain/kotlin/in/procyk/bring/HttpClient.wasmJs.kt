package `in`.procyk.bring

import io.ktor.client.engine.*
import io.ktor.client.engine.js.*

internal actual fun platformHttpEngineFactory(): HttpClientEngineFactory<*> = Js
