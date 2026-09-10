package com.example.androidproject1.feature.auth.presentation.login

import com.example.androidproject1.feature.auth.domain.AuthService
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.ui.event.SystemEvent
import com.example.androidproject1.service.core.ui.event.UiCommand
import com.example.androidproject1.service.core.ui.form.ALERT_ID_DISCARD
import com.example.androidproject1.service.core.ui.form.discardAlert
import com.example.androidproject1.service.core.ui.state.clearAlert
import com.example.androidproject1.service.core.ui.state.setAlert
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel

class LoginViewModel(
    logger: Logger,
    private val authService: AuthService,
) : BaseViewModel<LoginState, LoginEvent, LoginNavigation>(
    initialState = LoginState(),
    logger = logger.withTag("LoginViewModel"),
) {

    override fun onUiEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> updateData { copy(email = email.changed(event.email)) }

            is LoginEvent.PasswordChanged ->
                updateData { copy(password = password.changed(event.password)) }

            LoginEvent.LoginClicked -> login(email = uiState.value.data?.email?.value.orEmpty())

            LoginEvent.SkipLoginClicked -> login(email = "")

            LoginEvent.SignUpClicked -> navigate(LoginNavigation.SignUp)

            LoginEvent.BackRequested -> uiState.setAlert(discardAlert())
        }
    }

    override fun onSystemEvent(event: SystemEvent) {
        if (event is SystemEvent.AlertResult.Confirmed && event.id == ALERT_ID_DISCARD) {
            uiState.clearAlert()
            // Login is the root of the auth flow, so leaving it leaves the app.
            sendCommand(UiCommand.CloseApp)
            return
        }
        super.onSystemEvent(event)
    }

    private fun login(email: String) = execute(
        loading = overlay(),
        action = { authService.login(email) },
        onData = { logger.d { "Signed in" } },
    )
}
