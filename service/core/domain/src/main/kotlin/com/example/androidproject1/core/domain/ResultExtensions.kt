package com.example.androidproject1.core.domain

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.cancellation.CancellationException

/**
 * Coroutine-safe [Result.onFailure]: rethrows [CancellationException] instead of swallowing it,
 * so a cancelled coroutine is never mistaken for a failed call.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> Result<T>.onSafeFailure(action: (exception: Throwable) -> Unit): Result<T> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    exceptionOrNull()?.let {
        if (it is CancellationException) {
            throw it
        }
        action(it)
    }
    return this
}
