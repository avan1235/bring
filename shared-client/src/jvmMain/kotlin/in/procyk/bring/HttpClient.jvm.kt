package `in`.procyk.bring

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

internal actual fun platformHttpEngineFactory(): HttpClientEngineFactory<*> = CIO
