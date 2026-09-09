package com.example.androidproject1.feature.settings.presentation.permissions

import com.example.androidproject1.core.ui.event.UiEvent
import com.example.androidproject1.core.ui.permission.DeclaredPermission

sealed interface SettingsPermissionsEvent : UiEvent {

    /**
     * What the platform currently says, read by the screen on first composition and on every
     * resume.
     *
     * A permission lives outside the app and changes while it is in the background, so there is
     * nothing a ViewModel could observe — only something the UI can re-read. It arrives as an
     * event like any other, and the ViewModel stays free of a `Context`. The alternative, a
     * repository the ViewModel polls, is item 7.13 and is not needed until a ViewModel has to make
     * a decision on it.
     */
    data class PermissionsRead(val permissions: List<DeclaredPermission>) : SettingsPermissionsEvent

    data object OpenAppSettingsClicked : SettingsPermissionsEvent

    data object NavigateUpClicked : SettingsPermissionsEvent
}
