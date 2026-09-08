package com.example.androidproject1.feature.auth.data.source

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.androidproject1.core.data.DataStoreProvider
import com.example.androidproject1.core.domain.coroutines.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Switching to IO is this class's job, not the repository's: `BaseRepository` runs on the caller's
 * context and the caller is `viewModelScope`, which is `Dispatchers.Main`. Every data source that
 * touches disk or the network does the same.
 */
class DefaultLocalAuthDataSource(
    dataStoreProvider: DataStoreProvider,
    private val dispatcherProvider: DispatcherProvider,
) : LocalAuthDataSource {

    private val dataStore = dataStoreProvider.dataStore

    override fun observeEmail(): Flow<String?> =
        dataStore.data
            .map { it[KEY_EMAIL] }
            .flowOn(dispatcherProvider.io)

    override suspend fun setEmail(email: String?) = withContext(dispatcherProvider.io) {
        dataStore.edit { preferences ->
            if (email == null) preferences.remove(KEY_EMAIL) else preferences[KEY_EMAIL] = email
        }
        Unit
    }

    private companion object {

        val KEY_EMAIL = stringPreferencesKey("auth_email")
    }
}
