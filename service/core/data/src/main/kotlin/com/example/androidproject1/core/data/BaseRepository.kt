package com.example.androidproject1.core.data

import com.example.androidproject1.core.domain.DataResult
import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.domain.exception.DomainException
import com.example.androidproject1.core.domain.exception.InternalErrorException
import com.example.androidproject1.core.domain.onSafeFailure
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlin.coroutines.cancellation.CancellationException

/**
 * Base class for repository implementations. This is the single place where thrown exceptions are
 * turned into [DataResult.Error], so that everything above the infrastructure layer only ever
 * deals with [DataResult] and never with try/catch.
 */
abstract class BaseRepository(protected val logger: Logger) {

    /**
     * Runs a one-shot [call], wrapping success in [DataResult.Success] and any failure in
     * [DataResult.Error]. Cancellation propagates untouched.
     */
    protected suspend fun <T> repositoryCall(call: suspend () -> T): DataResult<T> =
        runCatching { DataResult.Success(call()) }
            .onSafeFailure { logger.w(throwable = it) { "Repository call failed" } }
            .getOrElse { DataResult.Error(it.toDomainException()) }

    /**
     * Wraps a one-shot [call] as a single-emission [Flow] of [DataResult].
     */
    protected fun <T> flowRepositoryCall(call: suspend () -> T): Flow<DataResult<T>> =
        flow { emit(repositoryCall(call)) }

    /**
     * Wraps an existing [Flow] so that its emissions become [DataResult.Success] and any thrown
     * exception becomes a [DataResult.Error].
     *
     * That error is necessarily **terminal** — a `Flow` that has thrown cannot be resumed, only
     * resubscribed. A long-lived flow whose collector outlives the failure (session state, say)
     * should therefore pass [retries], or one transient I/O error will stop it emitting for as
     * long as the collector is alive.
     *
     * @param retries how many times to resubscribe to [source] before giving up and emitting the
     * error. `0` keeps the plain fail-once behaviour.
     * @param retryDelayMillis how long to wait before each resubscription.
     */
    protected fun <T> flowRepositoryCall(
        source: Flow<T>,
        retries: Long = 0,
        retryDelayMillis: Long = DEFAULT_RETRY_DELAY_MILLIS,
    ): Flow<DataResult<T>> =
        source
            .map<T, DataResult<T>> { DataResult.Success(it) }
            .retryWhen { throwable, attempt ->
                val willRetry = throwable !is CancellationException && attempt < retries
                if (willRetry) {
                    logger.w(throwable = throwable) {
                        "Repository flow failed, retrying (${attempt + 1}/$retries)"
                    }
                    delay(retryDelayMillis)
                }
                willRetry
            }
            .catch { throwable ->
                if (throwable is CancellationException) throw throwable
                logger.w(throwable = throwable) { "Repository flow failed" }
                emit(DataResult.Error(throwable.toDomainException()))
            }

    private fun Throwable.toDomainException(): DomainException =
        this as? DomainException ?: InternalErrorException(cause = this)

    private companion object {

        const val DEFAULT_RETRY_DELAY_MILLIS = 500L
    }
}
