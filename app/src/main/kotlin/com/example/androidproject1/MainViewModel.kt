package com.example.androidproject1

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.feature.auth.domain.AuthService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

/** Single owner of the session, and the only thing that switches nav graphs. */
class MainViewModel(
    logger: Logger,
    authService: AuthService,
) : BaseViewModel<MainState, MainEvent, MainNavigation>(
    // Never rendered — MainViewModel owns no screen of its own (the app starts on the launch
    // screen instead). Non-null only so BaseViewModel doesn't start an overlay nothing shows.
    initialState = MainState(isLoggedIn = false),
    logger = logger.withTag("MainViewModel"),
) {

    // Tracks whether the first real decision has been made, since `data` starts non-null now and
    // can no longer stand in for "session not known yet".
    private var sessionKnown = false

    init {
        observe(
            // `onStart`'s delay stands in for real startup work (remote config, cache warm-up)
            // and is what keeps the launch screen up for a deliberate beat instead of a flash.
            flow = { authService.isLoggedIn().onStart { delay(MIN_LAUNCH_DURATION_MS) } },
            loading = {},
        ) { isLoggedIn ->
            val previous = uiState.value.data?.isLoggedIn
            uiState.update { it.copy(data = MainState(isLoggedIn = isLoggedIn)) }

            // Also fires on the very first decision: that is what carries the app on from the
            // launch screen, which never navigates itself — see LaunchNavigation.
            if (!sessionKnown || previous != isLoggedIn) {
                sessionKnown = true
                navigate(if (isLoggedIn) MainNavigation.Main else MainNavigation.Auth)
            }
        }
    }

    private companion object {

        const val MIN_LAUNCH_DURATION_MS = 2_000L
    }
}
