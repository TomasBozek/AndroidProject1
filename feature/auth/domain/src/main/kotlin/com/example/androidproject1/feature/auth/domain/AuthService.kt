package com.example.androidproject1.feature.auth.domain

import com.example.androidproject1.service.core.domain.result.Outcome
import com.example.androidproject1.service.core.domain.result.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** The auth feature's public API — the only part other features may depend on. */
interface AuthService {

    fun observeSession(): Flow<Outcome<Session?>>

    fun isLoggedIn(): Flow<Outcome<Boolean>>

    /** Mock sign-in: records the session locally, verifying nothing. */
    suspend fun login(email: String): Outcome<Unit>

    suspend fun logout(): Outcome<Unit>
}

class DefaultAuthService(
    private val authRepository: AuthRepository,
) : AuthService {

    override fun observeSession(): Flow<Outcome<Session?>> = authRepository.observeSession()

    override fun isLoggedIn(): Flow<Outcome<Boolean>> =
        authRepository.observeSession().map { result -> result.map { it != null } }

    override suspend fun login(email: String): Outcome<Unit> = authRepository.login(email)

    override suspend fun logout(): Outcome<Unit> = authRepository.logout()
}
