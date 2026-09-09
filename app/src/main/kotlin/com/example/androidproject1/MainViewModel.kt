package com.example.androidproject1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject1.core.domain.ErrorTracker
import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.domain.result.Outcome
import com.example.androidproject1.feature.auth.domain.AuthService
import com.example.androidproject1.feature.settings.domain.ThemePreference
import com.example.androidproject1.feature.settings.domain.ThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Single owner of the session and of the stored theme, and the only thing that switches between
 * the auth and main flows.
 *
 * A plain [ViewModel] rather than a `BaseViewModel`, because it is not a screen: there is no state
 * to render, no user event to receive, and nothing to put a loading overlay or an error dialog
 * over. Screens change the session and let this react — see `SettingsViewModel.logout()`.
 */
class MainViewModel(
    logger: Logger,
    private val authService: AuthService,
    private val themeRepository: ThemeRepository,
    private val errorTracker: ErrorTracker,
) : ViewModel() {

    private val logger = logger.withTag(TAG)

    private val mutableSessionState = MutableStateFlow<SessionState>(SessionState.Unknown)

    /** [SessionState.Unknown] until the stored session has been read once. */
    val sessionState: StateFlow<SessionState> = mutableSessionState.asStateFlow()

    private val mutableTheme = MutableStateFlow<ThemePreference?>(null)

    /**
     * The palette the whole app draws in, `null` until the stored choice has been read once.
     *
     * Nullable for the same reason [SessionState.Unknown] exists: a default applied before the
     * read lands is a light frame in front of someone who chose dark. The splash holds until
     * this is known, so nothing is drawn in the wrong palette and then swapped.
     */
    val theme: StateFlow<ThemePreference?> = mutableTheme.asStateFlow()

    init {
        observeSession()
        observeTheme()
    }

    // A member function, not an `init` body: a constructor parameter shadows the property of the
    // same name inside `init`, so `logger` there would be the untagged one.
    private fun observeSession() {
        viewModelScope.launch {
            authService.observeSession().collect { outcome ->
                mutableSessionState.value = when (outcome) {
                    is Outcome.Success -> {
                        // The only place that knows who is signed in, so the only place that can
                        // tell the tracker. The id is opaque — never the address, because a crash
                        // report is not the place for one — and null on sign-out, or the next
                        // person's reports are attributed to the last one.
                        errorTracker.setUser(outcome.data?.id)
                        if (outcome.data != null) SessionState.SignedIn else SessionState.SignedOut
                    }

                    // `observeSession()` has already retried, and there is no screen to put a
                    // dialog over. Signed out is the safe reading and the one flow from which the
                    // user can do something about it.
                    is Outcome.Failure -> {
                        logger.w { "Session unreadable (${outcome.error}); treating as signed out" }
                        errorTracker.setUser(null)
                        SessionState.SignedOut
                    }
                }
            }
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            themeRepository.observeTheme().collect { outcome ->
                mutableTheme.value = when (outcome) {
                    is Outcome.Success -> outcome.data

                    // The repository has already retried. A palette is not worth holding the
                    // splash screen over, so the default is applied and the app starts.
                    is Outcome.Failure -> {
                        logger.w { "Theme unreadable (${outcome.error}); using the default" }
                        ThemePreference.DEFAULT
                    }
                }
            }
        }
    }

    private companion object {

        const val TAG = "MainViewModel"
    }
}
