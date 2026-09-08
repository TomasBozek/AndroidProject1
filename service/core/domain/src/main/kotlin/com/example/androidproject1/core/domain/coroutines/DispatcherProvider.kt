package com.example.androidproject1.core.domain.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * The dispatchers a data source may switch to, injected rather than referenced statically so a test
 * can substitute its own.
 *
 * Switch at the boundary that actually blocks — a network call, a DataStore read, a DAO query —
 * rather than wrapping every repository method: [com.example.androidproject1.core.data.BaseRepository]
 * runs on the caller's context, and the caller is `viewModelScope`, which is `Dispatchers.Main`.
 *
 * `open` so a test can override the three properties with a single test dispatcher.
 */
open class DispatcherProvider {

    open val main: CoroutineDispatcher get() = Dispatchers.Main

    open val default: CoroutineDispatcher get() = Dispatchers.Default

    open val io: CoroutineDispatcher get() = Dispatchers.IO
}
