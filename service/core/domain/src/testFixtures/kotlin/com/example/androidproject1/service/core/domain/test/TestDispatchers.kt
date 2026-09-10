package com.example.androidproject1.service.core.domain.test

import com.example.androidproject1.service.core.domain.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Every dispatcher unconfined, so a `withContext(io)` inside a data source stays on the test's own
 * thread instead of escaping to a real one and making the assertions racy.
 *
 * Beside [DispatcherProvider] rather than in one module's test source set: every `data` module
 * that switches at its boundary needs it, and the second copy is how the two drift.
 *
 * `Dispatchers.Unconfined` rather than `UnconfinedTestDispatcher`: an `object` is constructed once,
 * so a test dispatcher here would carry a `TestCoroutineScheduler` of its own, and the first
 * `delay` inside a `withContext(io)` — the HTTP client's retry backoff is one — fails the test with
 * "Detected use of different schedulers" against the scheduler `runTest` installed. Unconfined
 * leaves every scheduling decision to the test's own dispatcher and has nothing of its own to
 * disagree with. The cost is that such a delay is real time, so code whose waiting is the point of
 * a test makes the wait injectable, as the retry plugin's `delay` is in `HttpClientRetryTest`.
 */
object TestDispatchers : DispatcherProvider {

    override val main: CoroutineDispatcher = Dispatchers.Unconfined

    override val io: CoroutineDispatcher = Dispatchers.Unconfined

    override val default: CoroutineDispatcher = Dispatchers.Unconfined
}
