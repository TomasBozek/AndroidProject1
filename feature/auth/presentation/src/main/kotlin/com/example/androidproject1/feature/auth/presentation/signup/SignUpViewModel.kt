package com.example.androidproject1.feature.auth.presentation.signup

import com.example.androidproject1.feature.auth.domain.AuthService
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel

class SignUpViewModel(
    logger: Logger,
    private val authService: AuthService,
) : BaseViewModel<SignUpState, SignUpEvent, SignUpNavigation>(
    initialState = SignUpState(),
    logger = logger.withTag("SignUpViewModel"),
) {

    override fun onUiEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.EmailChanged -> updateData { copy(email = email.changed(event.email)) }

            is SignUpEvent.PasswordChanged ->
                updateData { copy(password = password.changed(event.password)) }

            is SignUpEvent.ConfirmPasswordChanged ->
                updateData {
                    copy(confirmPassword = confirmPassword.changed(event.confirmPassword))
                }

            SignUpEvent.SignUpClicked -> signUp()

            SignUpEvent.LoginClicked -> navigate(SignUpNavigation.Login)
        }
    }

    // Mock sign-up: authService.login() already "records the session locally, verifying nothing",
    // so creating an account and signing in are the same call for now.
    private fun signUp() = execute(
        loading = overlay(),
        action = { authService.login(uiState.value.data?.email?.value.orEmpty()) },
        onData = { logger.d { "Signed up" } },
    )
}
