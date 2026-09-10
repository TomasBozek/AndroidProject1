package com.example.androidproject1.feature.settings.data.repository

import com.example.androidproject1.feature.settings.data.source.LocalThemeDataSource
import com.example.androidproject1.feature.settings.domain.ThemePreference
import com.example.androidproject1.feature.settings.domain.ThemeRepository
import com.example.androidproject1.service.core.data.BaseRepository
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

class DefaultThemeRepository(
    logger: Logger,
    private val localThemeDataSource: LocalThemeDataSource,
) : ThemeRepository, BaseRepository(logger = logger.withTag("DefaultThemeRepository")) {

    // `retries`, because the collector is the Activity's own theme and outlives any failure: an
    // `observe` failure is terminal, so one transient read error would freeze the palette for the
    // life of the process. Same reason `DefaultAuthRepository.observeSession()` passes it.
    override fun observeTheme(): Flow<Outcome<ThemePreference>> =
        observe(source = localThemeDataSource.observeTheme(), retries = RETRIES)

    override suspend fun setTheme(theme: ThemePreference): Outcome<Unit> = execute {
        localThemeDataSource.setTheme(theme)
    }

    private companion object {

        const val RETRIES = 3L
    }
}
