package com.example.androidproject1.feature.home.presentation

import com.example.androidproject1.core.ui.event.UiEvent

sealed interface HomeEvent : UiEvent {

    data object SettingsClicked : HomeEvent

    data object BrowseCatalogClicked : HomeEvent
}
