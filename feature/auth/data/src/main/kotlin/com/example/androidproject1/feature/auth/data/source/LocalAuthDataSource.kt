package com.example.androidproject1.feature.auth.data.source

import kotlinx.coroutines.flow.Flow

/**
 * Internal to the data layer: it sits beside its implementation, and nothing above
 * `:feature:auth:data` names it. What the rest of the app depends on is `AuthRepository`.
 */
interface LocalAuthDataSource {

    fun observeSession(): Flow<StoredSession?>

    suspend fun setSession(session: StoredSession?)
}

/** The stored shape, kept apart from `Session` in `domain` for the usual reason. */
data class StoredSession(
    val id: String,
    val email: String,
)
