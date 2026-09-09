package com.example.androidproject1.core.domain.test

import com.example.androidproject1.core.domain.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Every dispatcher unconfined, so a `withContext(io)` inside a data source stays on the test's own
 * thread instead of escaping to a real one and making the assertions racy.
 *
 * Beside [DispatcherProvider] rather than in one module's test source set: every `data` module
 * that switches at its boundary needs it, and the second copy is how the two drift.
 */
@OptIn(ExperimentalCoroutinesApi::class)
object TestDispatchers : DispatcherProvider {

    override val main: CoroutineDispatcher = Dispatchers.Unconfined

    override val io: CoroutineDispatcher = UnconfinedTestDispatcher()

    override val default: CoroutineDispatcher = UnconfinedTestDispatcher()
}
