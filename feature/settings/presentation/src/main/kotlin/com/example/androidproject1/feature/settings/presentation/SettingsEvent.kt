package com.example.androidproject1.feature.settings.presentation

import com.example.androidproject1.core.ui.event.UiEvent

sealed interface SettingsEvent : UiEvent {

    data object PermissionsClicked : SettingsEvent

    data object LogoutClicked : SettingsEvent
}
