package com.example.androidproject1.feature.settings.presentation

sealed interface SettingsDirection {

    data object NavigateUp : SettingsDirection
}
