package com.example.androidproject1.feature.settings.domain

import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

/**
 * Implemented in the data layer. Declared here so the domain layer depends on nothing.
 *
 * Observed rather than read once: two things render the choice — the Settings screen and the theme
 * wrapped around the whole app — and they live in different ViewModels. A one-shot read would
 * leave the root drawing the old palette until the next launch.
 */
interface ThemeRepository {

    /** [ThemePreference.DEFAULT] until something has been stored. */
    fun observeTheme(): Flow<Outcome<ThemePreference>>

    suspend fun setTheme(theme: ThemePreference): Outcome<Unit>
}
