package `in`.procyk.bring

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.rpc.annotations.Rpc
import kotlinx.rpc.krpc.ktor.client.KtorRpcClient
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.ktor.client.rpcConfig
import kotlinx.rpc.krpc.serialization.cbor.cbor
import kotlinx.rpc.withService
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.time.Duration.Companion.seconds

interface DurableRpcService<@Rpc T : Any> {

    fun durableCall(f: suspend T.() -> Unit)

    fun durableLaunch(f: suspend T.() -> Unit)
}

@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class, ExperimentalSerializationApi::class)
inline fun <@Rpc reified T : Any> DurableRpcService(
    coroutineScope: CoroutineScope,
    httpClient: HttpClient,
    crossinline configureRequest: HttpRequestBuilder.() -> Unit,
): DurableRpcService<T> = object : DurableRpcService<T> {
    private var client: KtorRpcClient? = null
    private val mutex = Mutex()

    private inline val KtorRpcClient.hasActiveConnection: Boolean
        get() = !webSocketSession.isCompleted || webSocketSession.getCompleted()
            .run { isActive && !outgoing.isClosedForSend }

    private val lastActiveClient: KtorRpcClient?
        get() = client?.takeIf { it.hasActiveConnection }

    private val callQueue = Channel<suspend T.() -> Unit>(UNLIMITED)
    private val launchQueue = Channel<suspend T.() -> Unit>(UNLIMITED)

    private suspend fun ensureActiveClient(): KtorRpcClient = lastActiveClient ?: mutex.withLock {
        lastActiveClient?.let { return@withLock it }
        httpClient.rpc {
            method = HttpMethod.Get
            rpcConfig {
                serialization {
                    cbor(DefaultCbor)
                }
            }
            configureRequest()
        }.also {
            client = it
        }
    }

    private suspend inline fun retry(crossinline f: suspend () -> Unit) {
        var duration = 0.25.seconds
        loop@ while (currentCoroutineContext().isActive) runCatching {
            f()
            break@loop
        }.onFailure {
            delay(duration)
            duration = minOf(4.seconds, duration * 2)
        }
    }

    private suspend inline fun <U> cancellable(crossinline f: suspend T.() -> U): U {
        return coroutineScope {
            val client = ensureActiveClient()
            val checkJob = launch {
                while (currentCoroutineContext().isActive) {
                    delay(1.seconds)
                    if (client.hasActiveConnection) continue
                    this@coroutineScope.cancel("websocket connection closed")
                }
            }
            client.withService<T>().f().also {
                checkJob.cancelAndJoin()
            }
        }
    }

    init {
        coroutineScope.launch {
            for (call in callQueue) retry {
                cancellable(call)
            }
        }
        coroutineScope.launch {
            for (launch in launchQueue) coroutineScope.launch {
                retry {
                    cancellable(launch)
                }
            }
        }
    }

    override fun durableCall(f: suspend T.() -> Unit) {
        callQueue.trySend(f)
    }

    override fun durableLaunch(f: suspend T.() -> Unit) {
        launchQueue.trySend(f)
    }
}