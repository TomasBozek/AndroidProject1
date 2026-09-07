package com.example.androidproject1.feature.auth.data

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.androidproject1.core.data.DataStoreProvider
import com.example.androidproject1.feature.auth.infrastructure.LocalAuthDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultLocalAuthDataSource(
    dataStoreProvider: DataStoreProvider,
) : LocalAuthDataSource {

    private val dataStore = dataStoreProvider.dataStore

    override fun observeEmail(): Flow<String?> = dataStore.data.map { it[KEY_EMAIL] }

    override suspend fun setEmail(email: String?) {
        dataStore.edit { preferences ->
            if (email == null) preferences.remove(KEY_EMAIL) else preferences[KEY_EMAIL] = email
        }
    }

    private companion object {

        val KEY_EMAIL = stringPreferencesKey("auth_email")
    }
}
