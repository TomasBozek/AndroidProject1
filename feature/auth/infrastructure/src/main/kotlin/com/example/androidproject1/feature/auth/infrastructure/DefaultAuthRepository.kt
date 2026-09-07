package com.example.androidproject1.feature.auth.infrastructure

import com.example.androidproject1.core.data.BaseRepository
import com.example.androidproject1.core.domain.DataResult
import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.feature.auth.domain.AuthRepository
import com.example.androidproject1.feature.auth.domain.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultAuthRepository(
    logger: Logger,
    private val localAuthDataSource: LocalAuthDataSource,
) : AuthRepository, BaseRepository(logger = logger.withTag("DefaultAuthRepository")) {

    // Retries because MainViewModel collects this for the whole process lifetime: without them a
    // single transient read failure would end the flow and stop the app reacting to sign-in/out.
    override fun observeSession(): Flow<DataResult<Session?>> =
        flowRepositoryCall(
            source = localAuthDataSource.observeEmail().map { email -> email?.let(::Session) },
            retries = SESSION_RETRIES,
        )

    override suspend fun login(email: String): DataResult<Unit> = repositoryCall {
        localAuthDataSource.setEmail(email.ifBlank { ANONYMOUS_EMAIL })
    }

    override suspend fun logout(): DataResult<Unit> = repositoryCall {
        localAuthDataSource.setEmail(null)
    }

    private companion object {

        /** Used when the user skips the login form rather than typing an address. */
        const val ANONYMOUS_EMAIL = "guest@example.com"

        const val SESSION_RETRIES = 3L
    }
}
