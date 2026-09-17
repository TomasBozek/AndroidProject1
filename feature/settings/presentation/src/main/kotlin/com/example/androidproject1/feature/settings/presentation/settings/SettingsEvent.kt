package com.example.androidproject1.feature.settings.presentation.settings

import com.example.androidproject1.feature.settings.domain.ThemePreference
import com.example.androidproject1.service.core.ui.event.UiEvent

sealed interface SettingsEvent : UiEvent {

    data object ProfileClicked : SettingsEvent

    data object PermissionsClicked : SettingsEvent

    /**
     * Whether this build has a debug menu, reported by the destination.
     *
     * A build fact rather than something the user did, handed over the way
     * `SettingsPermissionsEvent.PermissionsRead` hands over what the system said: the ViewModel
     * stays the only owner of the state, and the screen still renders from it alone.
     */
    data class DebugMenuAvailable(val available: Boolean) : SettingsEvent

    data object DebugMenuClicked : SettingsEvent

    /** The language picker, a screen of this feature. */
    data object LanguageClicked : SettingsEvent

    /** A palette picked on the segmented control; stored, and applied at the root. */
    data class ThemeSelected(val theme: ThemePreference) : SettingsEvent

    data object LogoutClicked : SettingsEvent
}
