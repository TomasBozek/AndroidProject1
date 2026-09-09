package com.example.androidproject1.feature.settings.presentation.permissions

/** One-off navigation intents, turned into back-stack calls in SettingsPermissionsDestination. */
sealed interface SettingsPermissionsNavigation {

    data object NavigateUp : SettingsPermissionsNavigation
}
