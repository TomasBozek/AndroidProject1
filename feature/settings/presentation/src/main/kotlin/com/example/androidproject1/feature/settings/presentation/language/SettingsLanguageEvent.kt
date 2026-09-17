package com.example.androidproject1.feature.settings.presentation.language

import com.example.androidproject1.feature.settings.domain.AppLanguage
import com.example.androidproject1.service.core.ui.event.UiEvent

sealed interface SettingsLanguageEvent : UiEvent {

    /** A ring tapped; stored, and applied by the platform at once. */
    data class LanguageSelected(val language: AppLanguage) : SettingsLanguageEvent

    data object NavigateUpClicked : SettingsLanguageEvent
}
