package com.example.androidproject1.feature.catalog.data.repository

import com.example.androidproject1.core.domain.coroutines.DispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Everything on one dispatcher, so `withContext(io)` inside the data source does not escape the
 * test's scheduler and make the assertions racy.
 */
internal object TestDispatchers : DispatcherProvider {
    override val main = Dispatchers.Unconfined
    override val io = UnconfinedTestDispatcher()
    override val default = UnconfinedTestDispatcher()
}
