package com.example.androidproject1.core.domain

import com.example.androidproject1.core.domain.exception.DomainException
import com.example.androidproject1.core.domain.exception.InternalErrorException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

/**
 * Result containing requested data of type [T], or an error wrapping a [DomainException].
 *
 * Everything crossing the domain boundary is expressed as a [DataResult] so that callers
 * never have to reason about which exceptions a repository might throw.
 */
sealed interface DataResult<out T> {

    data class Success<out T>(val data: T) : DataResult<T>

    data class Error(val exception: DomainException) : DataResult<Nothing>
}

/**
 * Wraps a failure as a [DataResult.Error], rethrowing [CancellationException] rather than
 * reporting a cancelled coroutine as an internal error.
 *
 * `runCatching` catches `CancellationException` too, so every combinator below has to filter it
 * back out — the same reason [onSafeFailure] exists.
 */
@PublishedApi
internal fun Throwable.toDataResultError(): DataResult.Error {
    if (this is CancellationException) throw this
    return DataResult.Error(this as? DomainException ?: InternalErrorException(cause = this))
}

inline fun <T, R> DataResult<T>.mapSuccessData(transform: (T) -> R): DataResult<R> =
    runCatching {
        when (this) {
            is DataResult.Error -> this
            is DataResult.Success<T> -> DataResult.Success(transform(data))
        }
    }.getOrElse { it.toDataResultError() }

inline fun <T, R> DataResult<T>.onSuccess(transform: (T) -> DataResult<R>): DataResult<R> =
    runCatching {
        when (this) {
            is DataResult.Error -> this
            is DataResult.Success<T> -> transform(data)
        }
    }.getOrElse { it.toDataResultError() }

inline fun <T> DataResult<T>.onError(action: (DomainException) -> DataResult<T>): DataResult<T> =
    runCatching {
        when (this) {
            is DataResult.Error -> action(exception)
            else -> this
        }
    }.getOrElse { it.toDataResultError() }

/**
 * Combines two result flows, short-circuiting on the first [DataResult.Error].
 */
fun <A, B> combineDataResults(
    flowA: Flow<DataResult<A>>,
    flowB: Flow<DataResult<B>>,
): Flow<DataResult<Pair<A, B>>> =
    combine(flowA, flowB) { a, b ->
        when {
            a is DataResult.Error -> a
            b is DataResult.Error -> b
            a is DataResult.Success && b is DataResult.Success -> DataResult.Success(a.data to b.data)
            else -> DataResult.Error(InternalErrorException(message = "Missing handling of DataResult flow combination."))
        }
    }

fun <A, B, C> combineDataResults(
    flowA: Flow<DataResult<A>>,
    flowB: Flow<DataResult<B>>,
    flowC: Flow<DataResult<C>>,
): Flow<DataResult<Triple<A, B, C>>> =
    combine(flowA, flowB, flowC) { a, b, c ->
        when {
            a is DataResult.Error -> a
            b is DataResult.Error -> b
            c is DataResult.Error -> c
            a is DataResult.Success && b is DataResult.Success && c is DataResult.Success ->
                DataResult.Success(Triple(a.data, b.data, c.data))
            else -> DataResult.Error(InternalErrorException(message = "Missing handling of DataResult flow combination."))
        }
    }

/**
 * Runs [secondBuilder] with the success value of [first], pairing both results.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <A, B> chainedDataResults(
    first: Flow<DataResult<A>>,
    secondBuilder: (A) -> Flow<DataResult<B>>,
): Flow<DataResult<Pair<A, B>>> =
    first.flatMapLatest { firstResult ->
        when (firstResult) {
            is DataResult.Success -> secondBuilder(firstResult.data).map { secondResult ->
                when (secondResult) {
                    is DataResult.Success -> DataResult.Success(firstResult.data to secondResult.data)
                    is DataResult.Error -> secondResult
                }
            }

            is DataResult.Error -> flowOf(firstResult)
        }
    }
