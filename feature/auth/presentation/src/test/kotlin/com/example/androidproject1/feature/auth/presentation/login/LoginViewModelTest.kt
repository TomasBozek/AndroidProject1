package com.example.androidproject1.feature.auth.presentation.login

import com.example.androidproject1.feature.auth.domain.test.FakeAuthService
import com.example.androidproject1.service.core.domain.error.UnauthorizedError
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.ui.event.SystemEvent
import com.example.androidproject1.service.core.ui.event.UiCommand
import com.example.androidproject1.service.core.ui.form.ALERT_ID_DISCARD
import com.example.androidproject1.service.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authService = FakeAuthService()

    private fun viewModel() = LoginViewModel(logger = FakeLogger(), authService = authService)

    @Test
    fun `submit stays disabled until both fields are filled`() = runTest {
        val viewModel = viewModel()

        assertFalse(viewModel.state.value.data!!.canSubmit)

        viewModel.onUiEvent(LoginEvent.EmailChanged("ada@example.com"))
        assertFalse(viewModel.state.value.data!!.canSubmit)

        viewModel.onUiEvent(LoginEvent.PasswordChanged("hunter2"))
        assertTrue(viewModel.state.value.data!!.canSubmit)
    }

    @Test
    fun `logging in signs the entered email in`() = runTest {
        val viewModel = viewModel()
        viewModel.onUiEvent(LoginEvent.EmailChanged("ada@example.com"))
        viewModel.onUiEvent(LoginEvent.PasswordChanged("hunter2"))

        viewModel.onUiEvent(LoginEvent.LoginClicked)

        assertEquals(listOf("ada@example.com"), authService.loggedInEmails)
    }

    @Test
    fun `skipping login signs in anonymously`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(LoginEvent.SkipLoginClicked)

        assertEquals(listOf(""), authService.loggedInEmails)
    }

    @Test
    fun `a failed sign-in raises an alert, not an inline error`() = runTest {
        authService.failWith = UnauthorizedError(message = "Wrong password")
        val viewModel = viewModel()
        viewModel.onUiEvent(LoginEvent.EmailChanged("ada@example.com"))
        viewModel.onUiEvent(LoginEvent.PasswordChanged("wrong"))

        viewModel.onUiEvent(LoginEvent.LoginClicked)

        // Alert: the screen behind it is perfectly renderable, so replacing it would be wrong.
        assertNotNull(viewModel.state.value.alert)
        assertEquals(null, viewModel.state.value.content)
    }

    /**
     * B1U4: the back gesture on a form with something in it asks before throwing it away. The
     * gesture itself is `DiscardBackHandler`'s job and is not testable off a device; what is worth
     * asserting is that the event raises the alert, and that confirming it actually leaves.
     */
    @Test
    fun `back on an empty form is not guarded, and on a typed one it asks`() = runTest {
        val viewModel = viewModel()

        assertFalse(viewModel.state.value.data!!.isDirty)

        viewModel.onUiEvent(LoginEvent.EmailChanged("a"))
        assertTrue(viewModel.state.value.data!!.isDirty)

        viewModel.onUiEvent(LoginEvent.BackRequested)
        assertEquals(ALERT_ID_DISCARD, viewModel.state.value.alert?.id)
    }

    @Test
    fun `discarding from Login closes the app, because Login is where the flow starts`() = runTest {
        val viewModel = viewModel()
        viewModel.onUiEvent(LoginEvent.EmailChanged("a"))
        viewModel.onUiEvent(LoginEvent.BackRequested)

        viewModel.onSystemEvent(SystemEvent.AlertResult.Confirmed(ALERT_ID_DISCARD, payload = null))

        assertNull(viewModel.state.value.alert)
        assertEquals(UiCommand.CloseApp, viewModel.command.first())
    }
}
