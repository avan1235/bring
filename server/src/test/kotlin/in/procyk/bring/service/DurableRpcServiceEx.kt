package `in`.procyk.bring.service

import arrow.core.Either
import arrow.core.Either.Left
import `in`.procyk.bring.DurableRpcService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.job
import kotlinx.rpc.annotations.Rpc
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

internal suspend inline fun <@Rpc reified T : Any, L, R> DurableRpcService<T>.testCall(
    crossinline f: suspend T.() -> Either<L, R>,
): L = coroutineScope {
    val result = CompletableDeferred<Either<L, R>>(parent = coroutineContext.job)
    durableCall {
        assertFalse(result.isCompleted)
        f().let(result::complete)
    }

    val value = result.await()

    assertIs<Left<L>>(value)

    value.value
}

internal suspend inline fun <@Rpc reified T : Any, L, R> DurableRpcService<T>.testLaunch(
    crossinline f: suspend T.() -> Flow<Either<L, R>>,
): Flow<Either<L, R>> = coroutineScope {
    val result = MutableSharedFlow<Either<L, R>>(replay = 1)
    val job = Job(coroutineContext.job)
    durableLaunch {
        assertEquals(0, result.subscriptionCount.value)
        f().collectIndexed { idx, value ->
            if (idx == 0) {
                job.complete()
            }
            result.emit(value)
        }
    }
    job.join()
    result.asSharedFlow()
}
