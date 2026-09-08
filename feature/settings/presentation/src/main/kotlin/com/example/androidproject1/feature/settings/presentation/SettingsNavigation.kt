package com.example.androidproject1.feature.settings.presentation

/** One-off navigation intents, turned into back-stack calls in SettingsDestination. */
sealed interface SettingsNavigation {

    data object Permissions : SettingsNavigation

    /** The component gallery. Another feature, so the destination takes a lambda for it. */
    data object Components : SettingsNavigation
}
