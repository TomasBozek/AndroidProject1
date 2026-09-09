package com.example.androidproject1

import com.example.androidproject1.core.domain.error.UnexpectedError
import com.example.androidproject1.core.domain.test.FakeErrorTracker
import com.example.androidproject1.core.domain.test.FakeLogger
import com.example.androidproject1.core.ui.test.MainDispatcherRule
import com.example.androidproject1.feature.auth.domain.Session
import com.example.androidproject1.feature.auth.domain.test.FakeAuthService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val logger = FakeLogger()
    private val authService = FakeAuthService()
    private val errorTracker = FakeErrorTracker()

    private fun viewModel() =
        MainViewModel(logger = logger, authService = authService, errorTracker = errorTracker)

    @Test
    fun `a stored session reads as signed in`() = runTest {
        authService.session.value = Session(id = "session-1", email = "ada@example.com")

        assertEquals(SessionState.SignedIn, viewModel().sessionState.value)
    }

    @Test
    fun `no stored session reads as signed out`() = runTest {
        assertEquals(SessionState.SignedOut, viewModel().sessionState.value)
    }

    @Test
    fun `signing out later switches the session back`() = runTest {
        authService.session.value = Session(id = "session-1", email = "ada@example.com")
        val viewModel = viewModel()

        authService.logout()

        assertEquals(SessionState.SignedOut, viewModel.sessionState.value)
    }

    @Test
    fun `signing in later switches the session forward`() = runTest {
        // The other direction of `signing out later switches the session back`: nothing but the
        // session changing moves the app between the auth flow and the tabs, so both ways have to
        // be asserted or half the switch is untested.
        val viewModel = viewModel()
        assertEquals(SessionState.SignedOut, viewModel.sessionState.value)

        authService.login("ada@example.com")

        assertEquals(SessionState.SignedIn, viewModel.sessionState.value)
        assertEquals(listOf(null, FakeAuthService.SESSION_ID), errorTracker.users)
    }

    @Test
    fun `a session that becomes unreadable mid-flight falls back to signed out`() = runTest {
        // `observeSession()` has already retried by the time a failure reaches here, and there is
        // no screen to put a dialog over — so the flow drops to the one state the user can act
        // from, and says so in the log rather than in their face.
        authService.session.value = Session(id = "session-1", email = "ada@example.com")
        val viewModel = viewModel()
        assertEquals(SessionState.SignedIn, viewModel.sessionState.value)

        authService.sessionError = UnexpectedError(message = "disk gone")
        authService.session.value = null
        advanceUntilIdle()

        assertEquals(SessionState.SignedOut, viewModel.sessionState.value)
        assertTrue(logger.warnings.isNotEmpty())
    }

    @Test
    fun `an unreadable session is signed out, with a warning rather than a dialog`() = runTest {
        authService.sessionError = UnexpectedError(message = "disk gone")

        assertEquals(SessionState.SignedOut, viewModel().sessionState.value)
        assertTrue(logger.warnings.isNotEmpty())
    }

    @Test
    fun `the session is unknown until it has been read`() = runTest {
        // Overrides the rule's UnconfinedTestDispatcher for this one case: under that dispatcher
        // the collection started in `init` has already produced a value by the time the
        // constructor returns, which is exactly what hides the state under test.
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = viewModel()

        assertEquals(SessionState.Unknown, viewModel.sessionState.value)

        advanceUntilIdle()
        assertEquals(SessionState.SignedOut, viewModel.sessionState.value)
    }

    @Test
    fun `a signed-in session is reported to the crash tracker by id`() = runTest {
        authService.session.value = Session(id = "session-1", email = "ada@example.com")

        viewModel()

        // The id, never the address: a crash report is not the place for one.
        assertEquals(listOf("session-1"), errorTracker.users)
    }

    @Test
    fun `signing out clears the reported user`() = runTest {
        authService.session.value = Session(id = "session-1", email = "ada@example.com")
        val viewModel = viewModel()

        authService.session.value = null
        advanceUntilIdle()

        // Null on the way out, or the next person's reports are attributed to the last one.
        assertEquals(listOf("session-1", null), errorTracker.users)
        assertEquals(SessionState.SignedOut, viewModel.sessionState.value)
    }

    @Test
    fun `an unreadable session clears the reported user too`() = runTest {
        authService.failWith = UnexpectedError()

        viewModel()

        assertEquals(listOf(null), errorTracker.users)
    }
}
