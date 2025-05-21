package `in`.procyk.bring.service

import arrow.core.Either
import arrow.core.right
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

inline fun <A, B> runCatchingAs(
    error: B,
    crossinline f: () -> Either<A, B>
): Either<A, B> {
    return runCatching { f() }.fold(
        onSuccess = { it },
        onFailure = { error.right() }
    )
}

suspend inline fun <A, B> runNewSuspendedTransactionCatchingAs(
    error: B,
    crossinline f: suspend Transaction.() -> Either<A, B>
): Either<A, B> {
    return runCatching { newSuspendedTransaction { f() } }.fold(
        onSuccess = { it },
        onFailure = { error.right() }
    )
}
