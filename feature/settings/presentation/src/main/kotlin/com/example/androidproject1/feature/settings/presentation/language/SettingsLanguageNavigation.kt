package com.example.androidproject1.feature.settings.presentation.language

/** One-off navigation intents, turned into back-stack calls in SettingsLanguageDestination. */
sealed interface SettingsLanguageNavigation {

    data object NavigateUp : SettingsLanguageNavigation
}
