package com.example.androidproject1.feature.auth.data.source

import com.example.androidproject1.core.data.EncryptedDataStoreProvider
import com.example.androidproject1.core.domain.coroutines.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * The session, encrypted at rest.
 *
 * It is in an [EncryptedDataStoreProvider] rather than the shared preferences store because it is
 * the one value here worth protecting: a copy of the app's data directory should not say who was
 * signed in. The store holds one string and the empty string means signed out — which is also what
 * an absent file and an undecryptable one look like, so there is no third case.
 *
 * Switching to IO is this class's job, not the repository's: `BaseRepository` runs on the caller's
 * context and the caller is `viewModelScope`, which is `Dispatchers.Main`. Every data source that
 * touches disk or the network does the same.
 */
class DefaultLocalAuthDataSource(
    sessionStoreProvider: EncryptedDataStoreProvider,
    private val dispatcherProvider: DispatcherProvider,
) : LocalAuthDataSource {

    private val dataStore = sessionStoreProvider.dataStore

    override fun observeEmail(): Flow<String?> =
        dataStore.data
            .map { it.ifEmpty { null } }
            .flowOn(dispatcherProvider.io)

    override suspend fun setEmail(email: String?) = withContext(dispatcherProvider.io) {
        dataStore.updateData { email.orEmpty() }
        Unit
    }
}
