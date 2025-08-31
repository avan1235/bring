package `in`.procyk.bring

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

internal actual fun platformHttpEngineFactory(): HttpClientEngineFactory<*> = Darwin
