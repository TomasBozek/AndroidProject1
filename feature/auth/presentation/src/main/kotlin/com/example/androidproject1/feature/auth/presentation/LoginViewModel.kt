package com.example.androidproject1.feature.auth.presentation

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.state.updateData
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.feature.auth.domain.AuthService

class LoginViewModel(
    logger: Logger,
    private val authService: AuthService,
) : BaseViewModel<LoginState, LoginEvent, LoginDirection>(
    initialState = LoginState(),
    logger = logger.withTag("LoginViewModel"),
) {

    override fun onUiEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> uiState.updateData { copy(email = event.email) }

            is LoginEvent.PasswordChanged -> uiState.updateData { copy(password = event.password) }

            LoginEvent.LoginClicked -> login(email = uiState.value.data?.email.orEmpty())

            LoginEvent.SkipLoginClicked -> login(email = "")
        }
    }

    private fun login(email: String) = domainCall(
        action = { authService.login(email) },
        handleData = { logger.d { "Signed in" } },
    )
}
