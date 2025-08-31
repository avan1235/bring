package `in`.procyk.bring

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.cbor.*
import kotlinx.rpc.krpc.ktor.client.installKrpc
import kotlinx.rpc.krpc.serialization.cbor.cbor
import kotlinx.serialization.ExperimentalSerializationApi
import io.ktor.client.HttpClient as KtorHttpClient

internal expect fun platformHttpEngineFactory(): HttpClientEngineFactory<*>

@OptIn(ExperimentalSerializationApi::class)
fun HttpClient(
    configure: HttpClientConfig<*>.() -> Unit = {},
): KtorHttpClient = KtorHttpClient(platformHttpEngineFactory()) {
    installKrpc {
        serialization {
            cbor(DefaultCbor)
        }
    }
    install(ContentNegotiation) {
        cbor(DefaultCbor)
    }
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(DefaultCbor)
    }
    install(Resources)
    followRedirects = true
    expectSuccess = true

    configure()
}
