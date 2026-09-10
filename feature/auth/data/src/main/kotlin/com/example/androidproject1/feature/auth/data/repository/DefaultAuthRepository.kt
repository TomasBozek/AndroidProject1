package com.example.androidproject1.feature.auth.data.repository

import com.example.androidproject1.feature.auth.data.source.LocalAuthDataSource
import com.example.androidproject1.feature.auth.data.source.StoredSession
import com.example.androidproject1.feature.auth.domain.AuthRepository
import com.example.androidproject1.feature.auth.domain.Session
import com.example.androidproject1.service.core.data.BaseRepository
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class DefaultAuthRepository(
    logger: Logger,
    private val localAuthDataSource: LocalAuthDataSource,
    /** Overridable so a test does not have to assert against a value it cannot predict. */
    private val newSessionId: () -> String = { UUID.randomUUID().toString() },
) : AuthRepository, BaseRepository(logger = logger.withTag("DefaultAuthRepository")) {

    // MainViewModel collects this for the whole process lifetime, so one transient read failure
    // would otherwise end the flow for good.
    override fun observeSession(): Flow<Outcome<Session?>> =
        observe(
            source = localAuthDataSource.observeSession().map { stored ->
                stored?.let { Session(id = it.id, email = it.email) }
            },
            retries = SESSION_RETRIES,
        )

    /**
     * Mints an opaque id at sign-in.
     *
     * A random id rather than a hash of the address: a hash is still the address to anyone holding
     * a list of addresses, and this one is going to a crash reporter.
     */
    override suspend fun login(email: String): Outcome<Unit> = execute {
        localAuthDataSource.setSession(
            StoredSession(id = newSessionId(), email = email.ifBlank { ANONYMOUS_EMAIL }),
        )
    }

    override suspend fun logout(): Outcome<Unit> = execute {
        localAuthDataSource.setSession(null)
    }

    private companion object {

        /** Used when the login form is skipped. */
        const val ANONYMOUS_EMAIL = "guest@example.com"

        const val SESSION_RETRIES = 3L
    }
}
