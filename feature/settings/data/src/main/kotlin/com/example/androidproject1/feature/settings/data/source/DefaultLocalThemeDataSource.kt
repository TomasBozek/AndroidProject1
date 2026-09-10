package com.example.androidproject1.feature.settings.data.source

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.androidproject1.feature.settings.domain.ThemePreference
import com.example.androidproject1.service.core.data.DataStoreProvider
import com.example.androidproject1.service.core.domain.coroutines.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Switching to IO is this class's job, not the repository's: `BaseRepository` runs on the caller's
 * context and the caller is `viewModelScope`, which is `Dispatchers.Main`.
 *
 * Stored as the enum's `name` rather than its ordinal: reordering the cases then renames nothing,
 * and a value written by an older build that no longer exists reads back as the default instead of
 * as whichever case happens to sit at that index now.
 */
class DefaultLocalThemeDataSource(
    dataStoreProvider: DataStoreProvider,
    private val dispatcherProvider: DispatcherProvider,
) : LocalThemeDataSource {

    private val dataStore = dataStoreProvider.dataStore

    override fun observeTheme(): Flow<ThemePreference> =
        dataStore.data
            .map { preferences -> preferences[KEY_THEME].toThemePreference() }
            // DataStore re-emits the whole preferences file on any edit, and every other feature
            // writes into the same one; without this, choosing an avatar would restart the theme.
            .distinctUntilChanged()
            .flowOn(dispatcherProvider.io)

    override suspend fun setTheme(theme: ThemePreference) = withContext(dispatcherProvider.io) {
        dataStore.edit { preferences -> preferences[KEY_THEME] = theme.name }
        Unit
    }

    private companion object {

        val KEY_THEME = stringPreferencesKey("settings_theme")

        /** An absent or unrecognised value is a first run, or a downgrade. Both mean the default. */
        fun String?.toThemePreference(): ThemePreference =
            ThemePreference.entries.firstOrNull { it.name == this } ?: ThemePreference.DEFAULT
    }
}
