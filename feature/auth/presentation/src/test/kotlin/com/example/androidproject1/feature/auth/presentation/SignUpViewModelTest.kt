package com.example.androidproject1.feature.auth.presentation

import com.example.androidproject1.core.ui.test.FakeLogger
import com.example.androidproject1.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SignUpViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authService = FakeAuthService()

    private fun viewModel() = SignUpViewModel(logger = FakeLogger(), authService = authService)

    @Test
    fun `submit requires the two passwords to match`() = runTest {
        val viewModel = viewModel()
        viewModel.onUiEvent(SignUpEvent.EmailChanged("ada@example.com"))
        viewModel.onUiEvent(SignUpEvent.PasswordChanged("hunter2"))
        viewModel.onUiEvent(SignUpEvent.ConfirmPasswordChanged("hunter3"))

        assertFalse(viewModel.state.value.data!!.canSubmit)

        viewModel.onUiEvent(SignUpEvent.ConfirmPasswordChanged("hunter2"))
        assertTrue(viewModel.state.value.data!!.canSubmit)
    }

    @Test
    fun `signing up signs the new account in`() = runTest {
        val viewModel = viewModel()
        viewModel.onUiEvent(SignUpEvent.EmailChanged("ada@example.com"))
        viewModel.onUiEvent(SignUpEvent.PasswordChanged("hunter2"))
        viewModel.onUiEvent(SignUpEvent.ConfirmPasswordChanged("hunter2"))

        viewModel.onUiEvent(SignUpEvent.SignUpClicked)

        assertEquals(listOf("ada@example.com"), authService.loggedInEmails)
    }
}
