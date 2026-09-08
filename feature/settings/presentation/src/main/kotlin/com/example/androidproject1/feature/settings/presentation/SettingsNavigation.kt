package com.example.androidproject1.feature.settings.presentation

/** One-off navigation intents, turned into back-stack calls in SettingsDestination. */
sealed interface SettingsNavigation {

    data object Permissions : SettingsNavigation
}
