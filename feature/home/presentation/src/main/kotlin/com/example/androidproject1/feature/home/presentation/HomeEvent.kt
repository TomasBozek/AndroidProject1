package com.example.androidproject1.feature.home.presentation

import com.example.androidproject1.core.ui.Event

sealed interface HomeEvent : Event {

    data object SettingsClicked : HomeEvent
}
