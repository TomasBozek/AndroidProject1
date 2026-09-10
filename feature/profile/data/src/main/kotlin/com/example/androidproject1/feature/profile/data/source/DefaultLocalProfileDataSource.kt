package com.example.androidproject1.feature.profile.data.source

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.androidproject1.feature.profile.domain.Profile
import com.example.androidproject1.service.core.data.DataStoreProvider
import com.example.androidproject1.service.core.domain.coroutines.DispatcherProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Switching to IO is this class's job, not the repository's: `BaseRepository` runs on the caller's
 * context and the caller is `viewModelScope`, which is `Dispatchers.Main`.
 */
class DefaultLocalProfileDataSource(
    dataStoreProvider: DataStoreProvider,
    private val dispatcherProvider: DispatcherProvider,
) : LocalProfileDataSource {

    private val dataStore = dataStoreProvider.dataStore

    override suspend fun read(): Profile = withContext(dispatcherProvider.io) {
        val preferences = dataStore.data.first()
        Profile(
            name = preferences[KEY_NAME].orEmpty(),
            email = preferences[KEY_EMAIL].orEmpty(),
            avatarUri = preferences[KEY_AVATAR_URI],
        )
    }

    override suspend fun writeDetails(name: String, email: String) {
        withContext(dispatcherProvider.io) {
            dataStore.edit { preferences ->
                preferences[KEY_NAME] = name
                preferences[KEY_EMAIL] = email
            }
        }
    }

    override suspend fun writeAvatarUri(uri: String) {
        withContext(dispatcherProvider.io) {
            dataStore.edit { preferences -> preferences[KEY_AVATAR_URI] = uri }
        }
    }

    private companion object {

        val KEY_NAME = stringPreferencesKey("profile_name")
        val KEY_EMAIL = stringPreferencesKey("profile_email")
        val KEY_AVATAR_URI = stringPreferencesKey("profile_avatar_uri")
    }
}
