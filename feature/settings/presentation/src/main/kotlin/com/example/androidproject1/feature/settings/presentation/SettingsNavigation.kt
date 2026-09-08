package com.example.androidproject1.feature.settings.presentation

sealed interface SettingsNavigation {

    data object NavigateUp : SettingsNavigation
}
