package com.example.androidproject1.feature.settings.presentation

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.event.SystemEvent
import com.example.androidproject1.core.ui.state.setAlert
import com.example.androidproject1.core.ui.text.toUiText
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.feature.auth.domain.AuthService
import kotlinx.coroutines.flow.update

class SettingsViewModel(
    logger: Logger,
    private val authService: AuthService,
) : BaseViewModel<SettingsState, SettingsEvent, SettingsNavigation>(
    initialState = SettingsState(email = null),
    logger = logger.withTag("SettingsViewModel"),
) {

    init {
        observe(
            flow = { authService.observeSession() },
            loading = {},
        ) { session ->
            uiState.update { it.copy(data = SettingsState(email = session?.email)) }
        }
    }

    override fun onUiEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.NavigateUpClicked -> navigate(SettingsNavigation.NavigateUp)

            SettingsEvent.LogoutClicked -> uiState.setAlert(
                id = ALERT_ID_LOGOUT,
                title = R.string.settings_logout_title.toUiText(),
                message = R.string.settings_logout_confirmation.toUiText(),
                confirmLabel = R.string.settings_logout_confirm.toUiText(),
                declineLabel = R.string.settings_logout_cancel.toUiText(),
            )
        }
    }

    /** Alert results arrive here tagged with the alert's id; delegate what you don't handle. */
    override fun onSystemEvent(event: SystemEvent) {
        when {
            event is SystemEvent.AlertResult &&
                event.id == ALERT_ID_LOGOUT &&
                event is SystemEvent.AlertResult.Confirmed -> {
                super.onSystemEvent(event)
                logout()
            }

            else -> super.onSystemEvent(event)
        }
    }

    private fun logout() = execute(
        action = { authService.logout() },
        onData = { logger.d { "Signed out" } },
    )

    private companion object {

        const val ALERT_ID_LOGOUT = "logout_confirmation"
    }
}
