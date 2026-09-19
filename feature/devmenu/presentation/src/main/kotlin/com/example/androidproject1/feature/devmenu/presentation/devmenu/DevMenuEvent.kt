package com.example.androidproject1.feature.devmenu.presentation.devmenu

import com.example.androidproject1.feature.devmenu.presentation.DevMenuJump
import com.example.androidproject1.service.core.ui.event.UiEvent

sealed interface DevMenuEvent : UiEvent {

    /** The fixture engine's "server is down" switch — see `OfflineSwitch`. */
    data class OfflineToggled(val offline: Boolean) : DevMenuEvent

    /** The fixture engine's "send TMDB requests to the real host" switch — see `ApiSwitch`. */
    data class RealApiToggled(val realApi: Boolean) : DevMenuEvent

    /** Sends a handled exception to whatever `ErrorTracker` this build binds. */
    data object CrashClicked : DevMenuEvent

    /** Posts a notification whose tap opens a product deep link — see `NotificationTester`. */
    data object NotificationClicked : DevMenuEvent

    /** A row of the jump list. The jump carries its own back-stack call, wired in `AppNavHost`. */
    data class JumpClicked(val jump: DevMenuJump) : DevMenuEvent

    /** The component playground, a screen of this feature. */
    data object PlaygroundClicked : DevMenuEvent

    data object NavigateUpClicked : DevMenuEvent
}
