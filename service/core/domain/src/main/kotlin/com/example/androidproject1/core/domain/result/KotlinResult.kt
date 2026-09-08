package com.example.androidproject1.core.domain.result

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.cancellation.CancellationException

/**
 * [Result.onFailure] that rethrows [CancellationException] instead of swallowing it, so a cancelled
 * coroutine is never mistaken for a failed call.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> Result<T>.onFailureUnlessCancelled(action: (Throwable) -> Unit): Result<T> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    exceptionOrNull()?.let {
        if (it is CancellationException) throw it
        action(it)
    }
    return this
}
