package com.example.androidproject1.feature.auth.presentation

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.state.updateData
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.feature.auth.domain.AuthService

class SignUpViewModel(
    logger: Logger,
    private val authService: AuthService,
) : BaseViewModel<SignUpState, SignUpEvent, SignUpNavigation>(
    initialState = SignUpState(),
    logger = logger.withTag("SignUpViewModel"),
) {

    override fun onUiEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.EmailChanged -> uiState.updateData { copy(email = event.email) }

            is SignUpEvent.PasswordChanged -> uiState.updateData { copy(password = event.password) }

            is SignUpEvent.ConfirmPasswordChanged ->
                uiState.updateData { copy(confirmPassword = event.confirmPassword) }

            SignUpEvent.SignUpClicked -> signUp()

            SignUpEvent.LoginClicked -> navigate(SignUpNavigation.Login)
        }
    }

    // Mock sign-up: authService.login() already "records the session locally, verifying nothing",
    // so creating an account and signing in are the same call for now.
    private fun signUp() = execute(
        action = { authService.login(uiState.value.data?.email.orEmpty()) },
        onData = { logger.d { "Signed up" } },
    )
}
