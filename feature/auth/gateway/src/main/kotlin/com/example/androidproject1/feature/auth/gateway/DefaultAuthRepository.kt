package com.example.androidproject1.feature.auth.gateway

import com.example.androidproject1.core.data.BaseRepository
import com.example.androidproject1.core.domain.result.Outcome
import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.feature.auth.domain.AuthRepository
import com.example.androidproject1.feature.auth.domain.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultAuthRepository(
    logger: Logger,
    private val localAuthDataSource: LocalAuthDataSource,
) : AuthRepository, BaseRepository(logger = logger.withTag("DefaultAuthRepository")) {

    // MainViewModel collects this for the whole process lifetime, so one transient read failure
    // would otherwise end the flow for good.
    override fun observeSession(): Flow<Outcome<Session?>> =
        observe(
            source = localAuthDataSource.observeEmail().map { email -> email?.let(::Session) },
            retries = SESSION_RETRIES,
        )

    override suspend fun login(email: String): Outcome<Unit> = execute {
        localAuthDataSource.setEmail(email.ifBlank { ANONYMOUS_EMAIL })
    }

    override suspend fun logout(): Outcome<Unit> = execute {
        localAuthDataSource.setEmail(null)
    }

    private companion object {

        /** Used when the login form is skipped. */
        const val ANONYMOUS_EMAIL = "guest@example.com"

        const val SESSION_RETRIES = 3L
    }
}
