package com.example.androidproject1.feature.auth.domain

import com.example.androidproject1.core.domain.DataResult
import com.example.androidproject1.core.domain.mapSuccessData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The auth feature's public API. Other features depend on this interface (and only on it) — for
 * example `:feature:settings:presentation` uses it to sign out.
 */
interface AuthService {

    fun observeSession(): Flow<DataResult<Session?>>

    fun isLoggedIn(): Flow<DataResult<Boolean>>

    /** Mock sign-in: records the session locally, no credentials are verified. */
    suspend fun login(email: String): DataResult<Unit>

    suspend fun logout(): DataResult<Unit>
}

class DefaultAuthService(
    private val authRepository: AuthRepository,
) : AuthService {

    override fun observeSession(): Flow<DataResult<Session?>> = authRepository.observeSession()

    override fun isLoggedIn(): Flow<DataResult<Boolean>> =
        authRepository.observeSession().map { result -> result.mapSuccessData { it != null } }

    override suspend fun login(email: String): DataResult<Unit> = authRepository.login(email)

    override suspend fun logout(): DataResult<Unit> = authRepository.logout()
}
