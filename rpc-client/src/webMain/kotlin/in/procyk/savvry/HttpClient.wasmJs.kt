package `in`.procyk.savvry

import io.ktor.client.engine.*
import io.ktor.client.engine.js.*

internal actual fun platformHttpEngineFactory(): HttpClientEngineFactory<*> = Js
