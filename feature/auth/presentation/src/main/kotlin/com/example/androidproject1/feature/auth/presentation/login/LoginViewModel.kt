package com.example.androidproject1.feature.auth.presentation.login

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.state.updateData
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.feature.auth.domain.AuthService

class LoginViewModel(
    logger: Logger,
    private val authService: AuthService,
) : BaseViewModel<LoginState, LoginEvent, LoginNavigation>(
    initialState = LoginState(),
    logger = logger.withTag("LoginViewModel"),
) {

    override fun onUiEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> uiState.updateData { copy(email = email.changed(event.email)) }

            is LoginEvent.PasswordChanged ->
                uiState.updateData { copy(password = password.changed(event.password)) }

            LoginEvent.LoginClicked -> login(email = uiState.value.data?.email?.value.orEmpty())

            LoginEvent.SkipLoginClicked -> login(email = "")

            LoginEvent.SignUpClicked -> navigate(LoginNavigation.SignUp)
        }
    }

    private fun login(email: String) = execute(
        action = { authService.login(email) },
        onData = { logger.d { "Signed in" } },
    )
}
