package com.example.androidproject1.feature.settings.data.source

import com.example.androidproject1.feature.settings.domain.ThemePreference
import kotlinx.coroutines.flow.Flow

/**
 * Internal to the data layer: it sits beside its implementation, and nothing above
 * `:feature:settings:data` names it. What the rest of the app depends on is the repository
 * interface in `domain`.
 */
interface LocalThemeDataSource {

    /** [ThemePreference.DEFAULT] when nothing has been stored — a first run is not a failure. */
    fun observeTheme(): Flow<ThemePreference>

    suspend fun setTheme(theme: ThemePreference)
}
