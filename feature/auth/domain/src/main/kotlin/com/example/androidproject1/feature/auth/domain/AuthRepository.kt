package com.example.androidproject1.feature.auth.domain

import com.example.androidproject1.core.domain.DataResult
import kotlinx.coroutines.flow.Flow

/**
 * Implemented in the infrastructure layer. Declared here so the domain layer depends on nothing.
 */
interface AuthRepository {

    fun observeSession(): Flow<DataResult<Session?>>

    suspend fun login(email: String): DataResult<Unit>

    suspend fun logout(): DataResult<Unit>
}
