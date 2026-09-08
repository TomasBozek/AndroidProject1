package com.example.androidproject1.feature.auth.presentation

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
 * Lives here rather than in each test because two ViewModels need it. Once `:feature:auth:domain`
 * publishes test fixtures (plan item F6), this moves there and `:feature:settings` can use it too.
 */
class FakeAuthService(var failWith: DomainError? = null) : AuthService {

    val loggedInEmails = mutableListOf<String>()

    private val session = MutableStateFlow<Session?>(null)

    override fun observeSession(): Flow<Outcome<Session?>> = session.map { Outcome.Success(it) }

    override fun isLoggedIn(): Flow<Outcome<Boolean>> = session.map { Outcome.Success(it != null) }

    override suspend fun login(email: String): Outcome<Unit> {
        failWith?.let { return Outcome.Failure(it) }
        loggedInEmails += email
        session.value = Session(email = email.ifBlank { "guest@example.com" })
        return Outcome.Success(Unit)
    }

    override suspend fun logout(): Outcome<Unit> {
        failWith?.let { return Outcome.Failure(it) }
        session.value = null
        return Outcome.Success(Unit)
    }
}
