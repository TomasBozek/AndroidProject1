package com.example.androidproject1.core.ui.viewmodel

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.domain.error.NotFoundError
import com.example.androidproject1.core.domain.result.Outcome
import com.example.androidproject1.core.domain.test.FakeLogger
import com.example.androidproject1.core.ui.event.SystemEvent
import com.example.androidproject1.core.ui.event.UiEvent
import com.example.androidproject1.core.ui.text.UiText
import com.example.androidproject1.core.ui.text.toUiText
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

    private sealed interface TestEvent : UiEvent {
        data object Tapped : TestEvent
    }

    private sealed interface TestNavigation {
        data object Next : TestNavigation
    }

    /** Exposes the protected members so the base class can be driven from a test. */
    private class TestViewModel(
        initialState: TestState?,
    ) : BaseViewModel<TestState, TestEvent, TestNavigation>(
        initialState = initialState,
        logger = FakeLogger(),
    ) {

        fun <T> oneShot(
            loadingMessage: UiText? = null,
            action: suspend () -> Outcome<T>,
        ) = execute(loadingMessage = loadingMessage, action = action, onData = {})

        /** An [ErrorDisplay.Inline] call, which is the only kind that registers a retry. */
        fun <T> inlineLoad(contentId: String, action: suspend () -> Outcome<T>) = execute(
            alertId = contentId,
            errorDisplay = ErrorDisplay.Inline,
            action = action,
            onData = {},
        )

        fun <T> stream(flow: () -> Flow<Outcome<T>>) =
            observe(flow = { flow() }, onData = {})

        fun go(navigation: TestNavigation) = navigate(navigation)
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
        val source = MutableSharedFlow<Outcome<String>>(replay = 1)
        source.emit(Outcome.Failure(NotFoundError(message = "gone")))

        viewModel.stream { source }

        assertNull(viewModel.state.value.loading)
        assertNotNull(viewModel.state.value.alert)
    }

    @Test
    fun `a success from a long-lived flow clears the loading overlay`() = runTest {
        val viewModel = TestViewModel(TestState("ready"))
        val source = MutableSharedFlow<Outcome<String>>(replay = 1)
        source.emit(Outcome.Success("value"))

        viewModel.stream { source }

        assertNull(viewModel.state.value.loading)
        assertNull(viewModel.state.value.alert)
    }

    /** Regression: a flag rather than a count let a short call dismiss a long call's overlay. */
    @Test
    fun `overlapping calls keep the overlay up until the last one finishes`() = runTest {
        val viewModel = TestViewModel(TestState("ready"))
        val slow = CompletableDeferred<Unit>()
        val quick = CompletableDeferred<Unit>()

        viewModel.oneShot { slow.await(); Outcome.Success(Unit) }
        viewModel.oneShot { quick.await(); Outcome.Success(Unit) }
        assertNotNull(viewModel.state.value.loading)

        quick.complete(Unit)
        assertNotNull("the slow call is still running", viewModel.state.value.loading)

        slow.complete(Unit)
        assertNull(viewModel.state.value.loading)
    }

    /**
     * Regression: the overlay was rebuilt from a bare `LoadingState()` on every change, so a second
     * call starting — or finishing — threw away the wording the first one asked for.
     */
    @Test
    fun `a loading message survives another call starting and finishing`() = runTest {
        val viewModel = TestViewModel(TestState("ready"))
        val message = "Signing in".toUiText()
        val slow = CompletableDeferred<Unit>()
        val quick = CompletableDeferred<Unit>()

        viewModel.oneShot(loadingMessage = message) { slow.await(); Outcome.Success(Unit) }
        assertEquals(message, viewModel.state.value.loading?.message)

        viewModel.oneShot { quick.await(); Outcome.Success(Unit) }
        assertEquals("a message-less call must not blank the wording", message, viewModel.state.value.loading?.message)

        quick.complete(Unit)
        assertEquals(message, viewModel.state.value.loading?.message)

        slow.complete(Unit)
        assertNull(viewModel.state.value.loading)
    }

    // --- errors ---

    @Test
    fun `a domain error becomes an alert that is titled as an error`() = runTest {
        val viewModel = TestViewModel(TestState("ready"))

        viewModel.oneShot { Outcome.Failure(NotFoundError(message = "gone")) }

        val alert = viewModel.state.value.alert
        assertNotNull(alert)
        assertEquals(BaseViewModel.ALERT_ID_ERROR, alert!!.id)
        assertNotNull("the error path supplies the error title", alert.title)
    }

    /**
     * Regression: the retry was a single lambda slot, so with two inline loads in flight it always
     * held the call that started last. Retrying the first failure re-ran the second call.
     */
    @Test
    fun `two concurrent inline failures each retry their own call`() = runTest {
        val viewModel = TestViewModel(TestState("ready"))
        var firstRuns = 0
        var secondRuns = 0

        viewModel.inlineLoad("first") {
            firstRuns++
            Outcome.Failure(NotFoundError(message = "gone"))
        }
        viewModel.inlineLoad("second") {
            secondRuns++
            Outcome.Failure(NotFoundError(message = "gone"))
        }
        assertEquals(1, firstRuns)
        assertEquals(1, secondRuns)

        viewModel.onSystemEvent(SystemEvent.ContentAction("first"))

        assertEquals("the first call is the one that runs again", 2, firstRuns)
        assertEquals(1, secondRuns)

        viewModel.onSystemEvent(SystemEvent.ContentAction("second"))

        assertEquals(2, secondRuns)
    }

    @Test
    fun `a content action for an unknown id clears the content and re-runs nothing`() = runTest {
        val viewModel = TestViewModel(TestState("ready"))
        var runs = 0

        viewModel.inlineLoad("first") {
            runs++
            Outcome.Failure(NotFoundError(message = "gone"))
        }
        assertNotNull(viewModel.state.value.content)

        viewModel.onSystemEvent(SystemEvent.ContentAction("nobody"))

        assertNull(viewModel.state.value.content)
        assertEquals(1, runs)

        // The retry registered for "first" is untouched by an action meant for another id.
        viewModel.onSystemEvent(SystemEvent.ContentAction("first"))
        assertEquals(2, runs)
    }

    @Test
    fun `a retry is forgotten once its call succeeds`() = runTest {
        val viewModel = TestViewModel(TestState("ready"))
        var runs = 0

        viewModel.inlineLoad("first") {
            runs++
            if (runs == 1) Outcome.Failure(NotFoundError(message = "gone")) else Outcome.Success(Unit)
        }
        viewModel.onSystemEvent(SystemEvent.ContentAction("first"))
        assertEquals("the retry re-ran the call, and this time it succeeded", 2, runs)

        // Nothing is pending any more, so a stray action re-runs nothing and holds no closures.
        viewModel.onSystemEvent(SystemEvent.ContentAction("first"))

        assertEquals(2, runs)
    }

    // --- one-shot events ---

    /**
     * Regression: these used to be a MutableSharedFlow with no replay and no buffer, which silently
     * discarded anything emitted while the screen was not collecting.
     */
    @Test
    fun `a navigation emitted before anything collects is still delivered`() = runTest {
        val viewModel = TestViewModel(TestState("ready"))

        viewModel.go(TestNavigation.Next)

        assertEquals(TestNavigation.Next, viewModel.navigation.first())
    }
}
