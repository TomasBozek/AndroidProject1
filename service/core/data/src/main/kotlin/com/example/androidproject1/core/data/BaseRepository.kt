package com.example.androidproject1.core.data

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.domain.error.DomainError
import com.example.androidproject1.core.domain.error.UnexpectedError
import com.example.androidproject1.core.domain.result.Outcome
import com.example.androidproject1.core.domain.result.onFailureUnlessCancelled
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.transform
import kotlin.coroutines.cancellation.CancellationException

/**
 * Base class for repository implementations — the single place thrown exceptions become an
 * [Outcome.Failure], so nothing above the data layer needs try/catch.
 */
abstract class BaseRepository(protected val logger: Logger) {

    /** Runs a one-shot [call] as an [Outcome]. Cancellation propagates untouched. */
    protected suspend fun <T> execute(call: suspend () -> T): Outcome<T> =
        runCatching { Outcome.Success(call()) }
            .onFailureUnlessCancelled { logger.w(throwable = it) { "Repository call failed" } }
            .getOrElse { Outcome.Failure(it.asDomainError()) }

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

    /**
     * Cache first, then the network, then whatever the cache holds afterwards.
     *
     * The shape every remote-backed screen needs, so it is here rather than reimplemented per
     * feature. A collector sees, in order: what the cache already had (if anything), then the
     * cache again once [remote] has been fetched and [write] has stored it, and then every later
     * change to the cache for as long as it collects.
     *
     * **A remote failure over a stale cache emits both.** The cached data has already been
     * emitted as a [Outcome.Success], and the failure follows it — so a screen using
     * `ErrorDisplay.Inline` shows the stale content with a retry beside it rather than replacing
     * a working screen with an error. With nothing cached, only the failure is emitted, and the
     * screen has an error state to show because it has nothing else.
     *
     * @param local the cache. Any flow; `null` means "nothing cached yet", which is why it is
     * `T?` rather than `T` — an empty list is a cached empty list, not a cache miss.
     * @param remote fetched once, on collection.
     * @param write stores what [remote] returned. [local] is expected to emit again as a result.
     */
    protected fun <T : Any> cached(
        local: Flow<T?>,
        remote: suspend () -> T,
        write: suspend (T) -> Unit,
    ): Flow<Outcome<T>> = flow {
        // A snapshot, not a collection: the cache is emitted now so the screen has something to
        // draw, and the live flow below takes over once the refresh is done.
        val cachedBeforeRefresh = runCatching { local.first() }
            .onFailureUnlessCancelled { logger.w(throwable = it) { "Reading the cache failed" } }
            .getOrNull()

        var lastEmitted: T? = cachedBeforeRefresh
        if (cachedBeforeRefresh != null) emit(Outcome.Success(cachedBeforeRefresh))

        val refresh = runCatching { write(remote()) }
            .onFailureUnlessCancelled { logger.w(throwable = it) { "Refreshing the cache failed" } }

        val error = refresh.exceptionOrNull()
        if (error != null) {
            if (error is CancellationException) throw error
            emit(Outcome.Failure(error.asDomainError()))
            // Nothing was written, so the cache below can only repeat what was already emitted.
            if (cachedBeforeRefresh != null) return@flow
        }

        // Everything the cache does from here on, including the value the refresh just wrote.
        // Compared against the last value *emitted*, not the last one the cache produced:
        // `distinctUntilChanged` starts fresh here and would repeat the snapshot above whenever
        // the refresh wrote a value equal to it.
        emitAll(
            local.transform { value ->
                if (value != null && value != lastEmitted) {
                    lastEmitted = value
                    emit(Outcome.Success(value))
                }
            },
        )
    }.catch { throwable ->
        if (throwable is CancellationException) throw throwable
        logger.w(throwable = throwable) { "Cached flow failed" }
        emit(Outcome.Failure(throwable.asDomainError()))
    }

    private fun Throwable.asDomainError(): DomainError =
        this as? DomainError ?: UnexpectedError(cause = this)

    private companion object {

        const val DEFAULT_RETRY_DELAY_MILLIS = 500L
    }
}
