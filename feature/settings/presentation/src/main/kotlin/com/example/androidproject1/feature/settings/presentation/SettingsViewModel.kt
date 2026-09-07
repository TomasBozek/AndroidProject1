package com.example.androidproject1.feature.settings.presentation

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.CommonEvent
import com.example.androidproject1.core.ui.state.setAlert
import com.example.androidproject1.core.ui.toText
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.feature.auth.domain.AuthService
import kotlinx.coroutines.flow.update

class SettingsViewModel(
    logger: Logger,
    private val authService: AuthService,
) : BaseViewModel<SettingsState, SettingsEvent, SettingsDirection>(
    initialState = SettingsState(email = null),
    logger = logger.withTag("SettingsViewModel"),
) {

    init {
        domainCall(
            flow = { authService.observeSession() },
            loading = {},
        ) { session ->
            uiState.update { it.copy(data = SettingsState(email = session?.email)) }
        }
    }

    override fun onUiEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.NavigateUpClicked -> navigate(SettingsDirection.NavigateUp)

            // A ViewModel-reachable string is an AppString, so no Context is needed here.
            SettingsEvent.LogoutClicked -> uiState.setAlert(
                id = ALERT_ID_LOGOUT,
                title = R.string.settings_logout_title.toText(),
                message = R.string.settings_logout_confirmation.toText(),
                primaryButton = R.string.settings_logout_confirm.toText(),
                secondaryButton = R.string.settings_logout_cancel.toText(),
            )
        }
    }

    /**
     * Alert results come back here rather than to [onUiEvent], tagged with the alert's id — which
     * is how a screen with several dialogs tells them apart. Anything not handled is delegated to
     * `super`, which just dismisses the alert.
     */
    override fun onCommonEvent(event: CommonEvent) {
        when {
            event is CommonEvent.AlertDialogAction &&
                event.id == ALERT_ID_LOGOUT &&
                event is CommonEvent.AlertDialogAction.PrimaryClicked -> {
                super.onCommonEvent(event)
                logout()
            }

            else -> super.onCommonEvent(event)
        }
    }

    private fun logout() = domainCall(
        action = { authService.logout() },
        handleData = { logger.d { "Signed out" } },
    )

    private companion object {

        const val ALERT_ID_LOGOUT = "logout_confirmation"
    }
}
