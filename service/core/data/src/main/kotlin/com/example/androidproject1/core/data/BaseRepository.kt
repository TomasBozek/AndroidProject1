package com.example.androidproject1.core.data

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.domain.error.DomainError
import com.example.androidproject1.core.domain.error.UnexpectedError
import com.example.androidproject1.core.domain.result.Outcome
import com.example.androidproject1.core.domain.result.onFailureUnlessCancelled
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlin.coroutines.cancellation.CancellationException

/**
 * Base class for repository implementations — the single place thrown exceptions become an
 * [Outcome.Failure], so nothing above the gateway layer needs try/catch.
 */
abstract class BaseRepository(protected val logger: Logger) {

    /** Runs a one-shot [call] as an [Outcome]. Cancellation propagates untouched. */
    protected suspend fun <T> execute(call: suspend () -> T): Outcome<T> =
        runCatching { Outcome.Success(call()) }
            .onFailureUnlessCancelled { logger.w(throwable = it) { "Repository call failed" } }
            .getOrElse { Outcome.Failure(it.asDomainError()) }

    /** Wraps a one-shot [call] as a single-emission [Flow]. */
    protected fun <T> executeAsFlow(call: suspend () -> T): Flow<Outcome<T>> =
        flow { emit(execute(call)) }

    /**
     * Wraps an existing [Flow] so its emissions become [Outcome.Success] and a thrown exception an
     * [Outcome.Failure].
     *
     * That failure is necessarily terminal — a `Flow` that has thrown can only be resubscribed, not
     * resumed. A flow whose collector outlives the failure (session state, say) must pass [retries],
     * or one transient I/O error stops it emitting for as long as the collector lives.
     *
     * @param retries how many times to resubscribe before giving up. `0` fails once.
     */
    protected fun <T> observe(
        source: Flow<T>,
        retries: Long = 0,
        retryDelayMillis: Long = DEFAULT_RETRY_DELAY_MILLIS,
    ): Flow<Outcome<T>> =
        source
            .map<T, Outcome<T>> { Outcome.Success(it) }
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
                emit(Outcome.Failure(throwable.asDomainError()))
            }

    private fun Throwable.asDomainError(): DomainError =
        this as? DomainError ?: UnexpectedError(cause = this)

    private companion object {

        const val DEFAULT_RETRY_DELAY_MILLIS = 500L
    }
}
