package com.example.androidproject1.feature.catalog.data.source

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.androidproject1.core.data.DataStoreProvider
import com.example.androidproject1.core.domain.coroutines.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Switching to IO is this class's job, not the repository's: `BaseRepository` runs on the caller's
 * context and the caller is `viewModelScope`, which is `Dispatchers.Main`.
 *
 * Stored as one newline-joined string rather than as a `stringSetPreferencesKey`: a set has no
 * order, and "most recent first" is the whole of what a recent-searches list is. A newline cannot
 * appear in a single-line search field, so it is a separator nothing has to escape.
 */
class DefaultLocalRecentSearchesDataSource(
    dataStoreProvider: DataStoreProvider,
    private val dispatcherProvider: DispatcherProvider,
) : LocalRecentSearchesDataSource {

    private val dataStore = dataStoreProvider.dataStore

    override fun observeRecents(): Flow<List<String>> =
        dataStore.data
            .map { preferences -> preferences[KEY_RECENTS].orEmpty().toRecents() }
            // Every feature writes into the same preferences file and DataStore re-emits all of it
            // on any edit; without this, changing the theme would restart the search flow.
            .distinctUntilChanged()
            .flowOn(dispatcherProvider.io)

    override suspend fun read(): List<String> = withContext(dispatcherProvider.io) {
        dataStore.data.first()[KEY_RECENTS].orEmpty().toRecents()
    }

    override suspend fun replace(recents: List<String>) = withContext(dispatcherProvider.io) {
        dataStore.edit { preferences -> preferences[KEY_RECENTS] = recents.joinToString(SEPARATOR) }
        Unit
    }

    private companion object {

        val KEY_RECENTS = stringPreferencesKey("catalog_recent_searches")

        const val SEPARATOR = "\n"

        fun String.toRecents(): List<String> = split(SEPARATOR).filter { it.isNotBlank() }
    }
}
