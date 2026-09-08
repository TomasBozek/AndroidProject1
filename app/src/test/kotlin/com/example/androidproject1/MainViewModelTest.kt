package com.example.androidproject1

import com.example.androidproject1.core.domain.error.UnexpectedError
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

    private fun viewModel() = MainViewModel(logger = logger, authService = authService)

    @Test
    fun `a stored session reads as signed in`() = runTest {
        authService.session.value = Session(email = "ada@example.com")

        assertEquals(SessionState.SignedIn, viewModel().sessionState.value)
    }

    @Test
    fun `no stored session reads as signed out`() = runTest {
        assertEquals(SessionState.SignedOut, viewModel().sessionState.value)
    }

    @Test
    fun `signing out later switches the session back`() = runTest {
        authService.session.value = Session(email = "ada@example.com")
        val viewModel = viewModel()

        authService.logout()

        assertEquals(SessionState.SignedOut, viewModel.sessionState.value)
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
}
