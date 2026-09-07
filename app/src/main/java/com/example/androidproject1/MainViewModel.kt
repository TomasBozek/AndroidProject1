package com.example.androidproject1

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.feature.auth.domain.AuthService
import kotlinx.coroutines.flow.update

/**
 * Owns the one piece of state that outlives any single screen: whether there is a session.
 *
 * It is the only place that decides which nav graph is current, so "am I signed in" and "which
 * graph am I on" can never disagree. Individual screens (login, settings) just change the
 * session and let this react.
 */
class MainViewModel(
    logger: Logger,
    authService: AuthService,
) : BaseViewModel<MainState, MainEvent, MainDirection>(
    // Null on purpose: the nav graph must not be chosen until the session is known, and `data`
    // staying null is what keeps Screen() from rendering (and flashing the wrong graph) until then.
    initialState = null,
    logger = logger.withTag("MainViewModel"),
) {

    init {
        domainCall(
            flow = { authService.isLoggedIn() },
            loading = {},
        ) { isLoggedIn ->
            val previous = uiState.value.data?.isLoggedIn

            uiState.update { it.copy(data = MainState(isLoggedIn = isLoggedIn), loading = null) }

            // The first emission only establishes the start graph; navigate on later changes.
            if (previous != null && previous != isLoggedIn) {
                navigate(if (isLoggedIn) MainDirection.Main else MainDirection.Auth)
            }
        }
    }
}
