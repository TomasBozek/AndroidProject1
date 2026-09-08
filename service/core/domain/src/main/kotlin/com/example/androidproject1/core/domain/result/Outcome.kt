package com.example.androidproject1.core.domain.result

import com.example.androidproject1.core.domain.error.DomainError
import com.example.androidproject1.core.domain.error.UnexpectedError
import kotlin.coroutines.cancellation.CancellationException

/**
 * The value of a domain call: either the requested data or a [DomainError].
 *
 * Everything crossing the domain boundary is an [Outcome], so callers never have to know which
 * exceptions a repository might throw.
 */
sealed interface Outcome<out T> {

    data class Success<out T>(val data: T) : Outcome<T>

    data class Failure(val error: DomainError) : Outcome<Nothing>
}

/**
 * Wraps a throwable as [Outcome.Failure], rethrowing [CancellationException] so a cancelled
 * coroutine is never reported as a failure. `runCatching` catches cancellation too, which is why
 * every combinator below has to filter it back out.
 */
@PublishedApi
internal fun Throwable.toFailure(): Outcome.Failure {
    if (this is CancellationException) throw this
    return Outcome.Failure(this as? DomainError ?: UnexpectedError(cause = this))
}

inline fun <T, R> Outcome<T>.map(transform: (T) -> R): Outcome<R> =
    runCatching {
        when (this) {
            is Outcome.Failure -> this
            is Outcome.Success<T> -> Outcome.Success(transform(data))
        }
    }.getOrElse { it.toFailure() }

inline fun <T, R> Outcome<T>.flatMap(transform: (T) -> Outcome<R>): Outcome<R> =
    runCatching {
        when (this) {
            is Outcome.Failure -> this
            is Outcome.Success<T> -> transform(data)
        }
    }.getOrElse { it.toFailure() }

inline fun <T> Outcome<T>.recover(action: (DomainError) -> Outcome<T>): Outcome<T> =
    runCatching {
        when (this) {
            is Outcome.Failure -> action(error)
            else -> this
        }
    }.getOrElse { it.toFailure() }
