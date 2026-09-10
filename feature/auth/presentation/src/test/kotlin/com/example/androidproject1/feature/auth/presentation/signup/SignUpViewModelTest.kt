package com.example.androidproject1.feature.auth.presentation.signup

import com.example.androidproject1.feature.auth.domain.test.FakeAuthService
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.ui.event.SystemEvent
import com.example.androidproject1.service.core.ui.form.ALERT_ID_DISCARD
import com.example.androidproject1.service.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
        viewModel.onUiEvent(SignUpEvent.PasswordChanged("hunter2!!"))
        viewModel.onUiEvent(SignUpEvent.ConfirmPasswordChanged("hunter3!!"))

        assertFalse(viewModel.state.value.data!!.canSubmit)

        viewModel.onUiEvent(SignUpEvent.ConfirmPasswordChanged("hunter2!!"))
        assertTrue(viewModel.state.value.data!!.canSubmit)
    }

    @Test
    fun `signing up signs the new account in`() = runTest {
        val viewModel = viewModel()
        viewModel.onUiEvent(SignUpEvent.EmailChanged("ada@example.com"))
        viewModel.onUiEvent(SignUpEvent.PasswordChanged("hunter2!!"))
        viewModel.onUiEvent(SignUpEvent.ConfirmPasswordChanged("hunter2!!"))

        viewModel.onUiEvent(SignUpEvent.SignUpClicked)

        assertEquals(listOf("ada@example.com"), authService.loggedInEmails)
    }

    /** B1U4: three fields is three things to lose, so back asks first. */
    @Test
    fun `back on a typed form asks, and discarding goes to Login`() = runTest {
        val viewModel = viewModel()

        assertFalse(viewModel.state.value.data!!.isDirty)

        viewModel.onUiEvent(SignUpEvent.PasswordChanged("hunter2"))
        assertTrue(viewModel.state.value.data!!.isDirty)

        viewModel.onUiEvent(SignUpEvent.BackRequested)
        assertEquals(ALERT_ID_DISCARD, viewModel.state.value.alert?.id)

        viewModel.onSystemEvent(SystemEvent.AlertResult.Confirmed(ALERT_ID_DISCARD, payload = null))
        assertNull(viewModel.state.value.alert)
        assertEquals(SignUpNavigation.Login, viewModel.navigation.first())
    }
}
