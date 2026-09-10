package com.example.androidproject1.service.core.domain.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * The dispatchers a data source may switch to, injected rather than referenced statically so a test
 * can substitute its own.
 *
 * Switch at the boundary that actually blocks — a network call, a DataStore read, a DAO query —
 * rather than wrapping every repository method: [com.example.androidproject1.service.core.data.BaseRepository]
 * runs on the caller's context, and the caller is `viewModelScope`, which is `Dispatchers.Main`.
 *
 * A test substitutes its own with a one-liner:
 *
 * ```kotlin
 * private class TestDispatchers(d: CoroutineDispatcher) : DispatcherProvider {
 *     override val main = d
 *     override val default = d
 *     override val io = d
 * }
 * ```
 */
interface DispatcherProvider {

    val main: CoroutineDispatcher

    val default: CoroutineDispatcher

    val io: CoroutineDispatcher
}

/** The real dispatchers. The only implementation outside tests. */
class DefaultDispatcherProvider : DispatcherProvider {

    override val main: CoroutineDispatcher get() = Dispatchers.Main

    override val default: CoroutineDispatcher get() = Dispatchers.Default

    override val io: CoroutineDispatcher get() = Dispatchers.IO
}
