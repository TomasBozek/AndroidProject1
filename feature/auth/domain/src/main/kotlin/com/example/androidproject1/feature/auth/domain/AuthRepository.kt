package com.example.androidproject1.feature.auth.domain

import com.example.androidproject1.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

/** Implemented in the gateway layer; declared here so domain depends on nothing. */
interface AuthRepository {

    fun observeSession(): Flow<Outcome<Session?>>

    suspend fun login(email: String): Outcome<Unit>

    suspend fun logout(): Outcome<Unit>
}
