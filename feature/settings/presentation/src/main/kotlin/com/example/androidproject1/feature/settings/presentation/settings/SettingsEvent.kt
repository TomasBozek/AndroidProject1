package com.example.androidproject1.feature.settings.presentation.settings

import com.example.androidproject1.core.ui.event.UiEvent

sealed interface SettingsEvent : UiEvent {

    data object ProfileClicked : SettingsEvent

    data object PermissionsClicked : SettingsEvent

    data object ComponentsClicked : SettingsEvent

    data object LogoutClicked : SettingsEvent
}
