package com.example.androidproject1.feature.settings.presentation.settings

/** One-off navigation intents, turned into back-stack calls in SettingsDestination. */
sealed interface SettingsNavigation {

    /** Another feature, so the destination takes a lambda for it. */
    data object Profile : SettingsNavigation

    data object Permissions : SettingsNavigation

    /** The debug menu, which is where the component gallery now lives. Another feature. */
    data object DebugMenu : SettingsNavigation
}
