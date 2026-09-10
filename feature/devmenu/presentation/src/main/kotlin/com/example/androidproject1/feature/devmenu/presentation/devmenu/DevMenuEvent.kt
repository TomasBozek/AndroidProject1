package com.example.androidproject1.feature.devmenu.presentation.devmenu

import com.example.androidproject1.service.core.ui.event.UiEvent

sealed interface DevMenuEvent : UiEvent {

    /** The fixture engine's "server is down" switch — see `OfflineSwitch`. */
    data class OfflineToggled(val offline: Boolean) : DevMenuEvent

    /** Sends a handled exception to whatever `ErrorTracker` this build binds. */
    data object CrashClicked : DevMenuEvent

    /** Posts a notification whose tap opens a product deep link — see `NotificationTester`. */
    data object NotificationClicked : DevMenuEvent

    data object ComponentsClicked : DevMenuEvent

    data object NavigateUpClicked : DevMenuEvent
}
