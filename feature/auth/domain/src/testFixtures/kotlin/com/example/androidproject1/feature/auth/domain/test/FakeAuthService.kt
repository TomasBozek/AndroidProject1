package com.example.androidproject1.feature.auth.domain.test

import com.example.androidproject1.core.domain.error.DomainError
import com.example.androidproject1.core.domain.result.Outcome
import com.example.androidproject1.feature.auth.domain.AuthService
import com.example.androidproject1.feature.auth.domain.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory [AuthService] for tests. Set [failWith] to make the next call fail.
 *
 * A fixture of `:feature:auth:domain` rather than of one test source set, because the session is
 * cross-cutting: `:feature:settings` reads it too, and had grown its own near-copy.
 *
 * @property session settable, so a test can start from a signed-in state without calling [login].
 * @property sessionError set to make the session flows emit a failure instead of the session. Kept
 * apart from [failWith] so a test of a failing sign-in still gets a readable session.
 */
class FakeAuthService(var failWith: DomainError? = null) : AuthService {

    val session = MutableStateFlow<Session?>(null)

    var sessionError: DomainError? = null

    val loggedInEmails = mutableListOf<String>()

    var logoutCount = 0
        private set

    override fun observeSession(): Flow<Outcome<Session?>> =
        session.map { sessionOutcome(it) }

    override fun isLoggedIn(): Flow<Outcome<Boolean>> =
        session.map { sessionOutcome(it != null) }

    override suspend fun login(email: String): Outcome<Unit> {
        failWith?.let { return Outcome.Failure(it) }
        loggedInEmails += email
        // A fixed id rather than a random one: a test that asserts what reached the crash
        // reporter needs a value it can name.
        session.value = Session(id = SESSION_ID, email = email.ifBlank { "guest@example.com" })
        return Outcome.Success(Unit)
    }

    override suspend fun logout(): Outcome<Unit> {
        failWith?.let { return Outcome.Failure(it) }
        logoutCount++
        session.value = null
        return Outcome.Success(Unit)
    }

    private fun <T> sessionOutcome(value: T): Outcome<T> =
        sessionError?.let { Outcome.Failure(it) } ?: Outcome.Success(value)

    companion object {

        const val SESSION_ID = "session-1"
    }
}
