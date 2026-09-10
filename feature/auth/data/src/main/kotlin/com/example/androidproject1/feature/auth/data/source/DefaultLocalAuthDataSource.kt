package com.example.androidproject1.feature.auth.data.source

import com.example.androidproject1.service.core.data.EncryptedDataStoreProvider
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.domain.coroutines.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * The session, encrypted at rest.
 *
 * It is in an [EncryptedDataStoreProvider] rather than the shared preferences store because it is
 * the one value here worth protecting: a copy of the app\'s data directory should not say who was
 * signed in.
 *
 * The store holds one string. An absent file, an undecryptable one and an empty one all read as
 * signed out, so there is no third case — and neither does a session written by a build with a
 * different format, which is what the version prefix is for: it is not parsed, it is not
 * recognised.
 */
class DefaultLocalAuthDataSource(
    sessionStoreProvider: EncryptedDataStoreProvider,
    private val dispatcherProvider: DispatcherProvider,
    logger: Logger,
) : LocalAuthDataSource {

    private val logger = logger.withTag("DefaultLocalAuthDataSource")
    private val dataStore = sessionStoreProvider.dataStore

    override fun observeSession(): Flow<StoredSession?> =
        dataStore.data
            .map { stored -> stored.takeIf { it.isNotEmpty() }?.let(::decode) }
            .flowOn(dispatcherProvider.io)

    override suspend fun setSession(session: StoredSession?) = withContext(dispatcherProvider.io) {
        dataStore.updateData { session?.let(::encode).orEmpty() }
        Unit
    }

    private fun encode(session: StoredSession) =
        listOf(VERSION.toString(), session.id, session.email).joinToString(SEPARATOR)

    /**
     * Returns null for anything this build does not recognise.
     *
     * The alternative — throwing — turns "you upgraded the app" into a crash loop on launch, and
     * the worst a null costs is one sign-in.
     */
    private fun decode(stored: String): StoredSession? {
        val parts = stored.split(SEPARATOR)
        if (parts.size != FIELD_COUNT || parts[0] != VERSION.toString()) {
            logger.w { "Session in an unrecognised format; signing out" }
            return null
        }
        return StoredSession(id = parts[1], email = parts[2])
    }

    private companion object {

        /** Bump when the stored shape changes; the previous format then reads as signed out. */
        const val VERSION = 1

        /** ASCII unit separator: not in a UUID, and not valid in an address. */
        const val SEPARATOR = "\u001F"

        const val FIELD_COUNT = 3
    }
}
