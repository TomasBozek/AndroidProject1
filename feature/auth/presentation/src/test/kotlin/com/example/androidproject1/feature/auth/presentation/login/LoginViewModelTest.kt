package com.example.androidproject1.feature.auth.presentation.login

import com.example.androidproject1.feature.auth.domain.test.FakeAuthService
import com.example.androidproject1.service.core.domain.error.UnauthorizedError
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
}
