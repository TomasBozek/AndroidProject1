package com.example.androidproject1.core.ui.viewmodel

import com.example.androidproject1.core.domain.DataResult
import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.domain.exception.NotFoundException
import com.example.androidproject1.core.ui.Event
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {

    private data class TestState(val value: String)

    private sealed interface TestEvent : Event {
        data object Tapped : TestEvent
    }

    private sealed interface TestDirection {
        data object Next : TestDirection
    }

    private class FakeLogger : Logger {
        override fun withTag(tag: String): Logger = this
        override fun d(throwable: Throwable?, message: () -> String) = Unit
        override fun w(throwable: Throwable?, message: () -> String) = Unit
        override fun e(throwable: Throwable?, message: () -> String) = Unit
    }

    /** Exposes the protected members so the base class can be driven from a test. */
    private class TestViewModel(
        initialState: TestState?,
    ) : BaseViewModel<TestState, TestEvent, TestDirection>(
        initialState = initialState,
        logger = FakeLogger(),
    ) {

        fun <T> oneShot(action: suspend () -> DataResult<T>) =
            domainCall(action = action, handleData = {})

        fun <T> observe(flow: () -> Flow<DataResult<T>>) =
            domainCall(flow = { flow() }, handleData = {})

        fun go(direction: TestDirection) = navigate(direction)
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- initial state ---

    @Test
    fun `a non-null initial state renders immediately with no loading overlay`() {
        val viewModel = TestViewModel(TestState("ready"))

        assertEquals(TestState("ready"), viewModel.state.value.data)
        assertNull(viewModel.state.value.loading)
    }

    @Test
    fun `a null initial state starts behind the loading overlay`() {
        val viewModel = TestViewModel(null)

        assertNull(viewModel.state.value.data)
        assertNotNull(viewModel.state.value.loading)
    }

    // --- loading ---

    /**
     * Regression: this overload exists for long-lived flows, which never complete, so clearing the
     * overlay only in the `finally` left an error emission showing a spinner behind the alert with
     * no way for the user to dismiss it.
     */
    @Test
    fun `an error from a long-lived flow clears the loading overlay`() = runTest {
        val viewModel = TestViewModel(TestState("ready"))
        // replay = 1 with no completion: exactly the shape of an observe-forever flow.
        val source = MutableSharedFlow<DataResult<String>>(replay = 1)
        source.emit(DataResult.Error(NotFoundException(message = "gone")))

        viewModel.observe { source }

        assertNull(viewModel.state.value.loading)
        assertNotNull(viewModel.state.value.alert)
    }

    @Test
    fun `a success from a long-lived flow clears the loading overlay`() = runTest {
        val viewModel = TestViewModel(TestState("ready"))
        val source = MutableSharedFlow<DataResult<String>>(replay = 1)
        source.emit(DataResult.Success("value"))

        viewModel.observe { source }

        assertNull(viewModel.state.value.loading)
        assertNull(viewModel.state.value.alert)
    }

    /** Regression: a flag rather than a count let a short call dismiss a long call's overlay. */
    @Test
    fun `overlapping calls keep the overlay up until the last one finishes`() = runTest {
        val viewModel = TestViewModel(TestState("ready"))
        val slow = CompletableDeferred<Unit>()
        val quick = CompletableDeferred<Unit>()

        viewModel.oneShot { slow.await(); DataResult.Success(Unit) }
        viewModel.oneShot { quick.await(); DataResult.Success(Unit) }
        assertNotNull(viewModel.state.value.loading)

        quick.complete(Unit)
        assertNotNull("the slow call is still running", viewModel.state.value.loading)

        slow.complete(Unit)
        assertNull(viewModel.state.value.loading)
    }

    // --- errors ---

    @Test
    fun `a domain error becomes an alert that is titled as an error`() = runTest {
        val viewModel = TestViewModel(TestState("ready"))

        viewModel.oneShot { DataResult.Error(NotFoundException(message = "gone")) }

        val alert = viewModel.state.value.alert
        assertNotNull(alert)
        assertEquals(BaseViewModel.ALERT_ID_DOMAIN_ERROR, alert!!.id)
        assertNotNull("the error path supplies the error title", alert.title)
    }

    // --- one-shot events ---

    /**
     * Regression: these used to be a MutableSharedFlow with no replay and no buffer, which silently
     * discarded anything emitted while the screen was not collecting.
     */
    @Test
    fun `a direction emitted before anything collects is still delivered`() = runTest {
        val viewModel = TestViewModel(TestState("ready"))

        viewModel.go(TestDirection.Next)

        assertEquals(TestDirection.Next, viewModel.direction.first())
    }
}
