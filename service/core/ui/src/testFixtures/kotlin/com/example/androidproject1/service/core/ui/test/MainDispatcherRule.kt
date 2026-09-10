package com.example.androidproject1.service.core.ui.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Replaces `Dispatchers.Main` for the duration of a test, so `viewModelScope` works off-device.
 *
 * Every ViewModel test needs this and none of them should write it:
 *
 * ```
 * @get:Rule val mainDispatcherRule = MainDispatcherRule()
 * ```
 *
 * Defaults to [UnconfinedTestDispatcher] so work launched in `init` has already run by the time the
 * constructor returns — which is what most ViewModel tests want to assert on. Pass a
 * `StandardTestDispatcher` when the test needs to control when that work happens.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
