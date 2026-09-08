package com.example.androidproject1.feature.settings.presentation

/** One-off navigation intents, turned into back-stack calls in SettingsPermissionsDestination. */
sealed interface SettingsPermissionsNavigation {

    data object NavigateUp : SettingsPermissionsNavigation
}
