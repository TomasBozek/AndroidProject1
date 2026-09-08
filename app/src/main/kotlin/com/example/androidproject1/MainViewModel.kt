package com.example.androidproject1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.domain.result.Outcome
import com.example.androidproject1.feature.auth.domain.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Single owner of the session, and the only thing that switches between the auth and main flows.
 *
 * A plain [ViewModel] rather than a `BaseViewModel`, because it is not a screen: there is no state
 * to render, no user event to receive, and nothing to put a loading overlay or an error dialog
 * over. Screens change the session and let this react — see `SettingsViewModel.logout()`.
 */
class MainViewModel(
    logger: Logger,
    private val authService: AuthService,
) : ViewModel() {

    private val logger = logger.withTag(TAG)

    private val mutableSessionState = MutableStateFlow<SessionState>(SessionState.Unknown)

    /** [SessionState.Unknown] until the stored session has been read once. */
    val sessionState: StateFlow<SessionState> = mutableSessionState.asStateFlow()

    init {
        observeSession()
    }

    // A member function, not an `init` body: a constructor parameter shadows the property of the
    // same name inside `init`, so `logger` there would be the untagged one.
    private fun observeSession() {
        viewModelScope.launch {
            authService.isLoggedIn().collect { outcome ->
                mutableSessionState.value = when (outcome) {
                    is Outcome.Success -> {
                        if (outcome.data) SessionState.SignedIn else SessionState.SignedOut
                    }

                    // `observeSession()` has already retried, and there is no screen to put a
                    // dialog over. Signed out is the safe reading and the one flow from which the
                    // user can do something about it.
                    is Outcome.Failure -> {
                        logger.w { "Session unreadable (${outcome.error}); treating as signed out" }
                        SessionState.SignedOut
                    }
                }
            }
        }
    }

    private companion object {

        const val TAG = "MainViewModel"
    }
}
