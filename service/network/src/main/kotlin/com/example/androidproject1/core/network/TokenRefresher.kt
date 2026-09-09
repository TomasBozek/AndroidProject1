package com.example.androidproject1.core.network

/**
 * Exchanges a refresh token for a new pair.
 *
 * Separate from [TokenStore] because storing and refreshing are different concerns with different
 * owners: the store is this app's persistence, the refresher is the API's contract. A project with
 * no refresh endpoint binds one that always returns null and keeps the store.
 */
interface TokenRefresher {

    /** Returns the new tokens, or `null` when the refresh token is no longer good. */
    suspend fun refresh(refreshToken: String): Tokens?
}

data class Tokens(
    val accessToken: String,
    val refreshToken: String?,
)
