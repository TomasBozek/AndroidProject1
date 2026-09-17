package com.example.androidproject1.feature.settings.domain

import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

/**
 * Implemented in `:app` (D75), because the store is the platform's own per-app language setting
 * and reaching it is an activity-stack concern. Declared here so the domain layer depends on
 * nothing and the Settings feature can read it.
 *
 * Observed rather than read once: the picker shows the choice, and a later screen may too.
 */
interface LanguageRepository {

    /** [AppLanguage.DEFAULT] until something has been stored. */
    fun observeLanguage(): Flow<Outcome<AppLanguage>>

    /** Applies at once — the platform recreates what it has to — and persists across launches. */
    suspend fun setLanguage(language: AppLanguage): Outcome<Unit>
}
