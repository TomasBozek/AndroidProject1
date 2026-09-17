package com.example.androidproject1.feature.settings.presentation.language

import com.example.androidproject1.feature.settings.domain.LanguageRepository
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel

class SettingsLanguageViewModel(
    logger: Logger,
    private val languageRepository: LanguageRepository,
) : BaseViewModel<SettingsLanguageState, SettingsLanguageEvent, SettingsLanguageNavigation>(
    initialState = SettingsLanguageState(),
    logger = logger.withTag("SettingsLanguageViewModel"),
) {

    init {
        // Observed, not read once: the ring follows the store, so a choice the store refused is
        // never shown as made.
        observe(
            flow = { languageRepository.observeLanguage() },
        ) { language ->
            updateData { copy(selected = language) }
        }
    }

    override fun onUiEvent(event: SettingsLanguageEvent) {
        when (event) {
            // No overlay and no optimistic update: below API 33 the platform recreates the
            // activity as soon as the locale is set, and above it every string repaints — either
            // way the user is watching the change land, and the flow above re-emits.
            is SettingsLanguageEvent.LanguageSelected -> execute(
                action = { languageRepository.setLanguage(event.language) },
                onData = {},
            )

            SettingsLanguageEvent.NavigateUpClicked -> navigate(SettingsLanguageNavigation.NavigateUp)
        }
    }
}
