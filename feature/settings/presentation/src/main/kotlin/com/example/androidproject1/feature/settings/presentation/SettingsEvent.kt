package com.example.androidproject1.feature.settings.presentation

import com.example.androidproject1.core.ui.Event

sealed interface SettingsEvent : Event {

    data object NavigateUpClicked : SettingsEvent

    data object LogoutClicked : SettingsEvent
}
