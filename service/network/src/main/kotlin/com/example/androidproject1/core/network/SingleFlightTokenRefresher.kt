package com.example.androidproject1.core.network

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * One refresh, however many requests hit 401 at once.
 *
 * Without this, a screen that fires three parallel requests on an expired token performs three
 * refreshes; two of them present a refresh token the first has already rotated, so the server
 * rejects them and the user is signed out in the middle of a working session.
 *
 * The mechanism is a mutex plus a comparison rather than a shared `Deferred`: a caller that was
 * waiting re-reads the store and, if the access token is no longer the stale one it arrived with,
 * takes what the winner stored instead of refreshing again. That needs no scope of its own, so a
 * cancelled caller cannot cancel the refresh another caller is waiting on.
 */
class SingleFlightTokenRefresher(
    private val tokenStore: TokenStore,
    private val tokenRefresher: TokenRefresher,
) {

    private val mutex = Mutex()

    /**
     * @param staleAccessToken the token whose request was rejected, or `null` if there was none.
     * @return the tokens now in the store, or `null` when the session is over — in which case the
     * store has been cleared.
     */
    suspend fun refresh(staleAccessToken: String?): Tokens? = mutex.withLock {
        val current = tokenStore.accessToken()
        if (current != null && current != staleAccessToken) {
            // Another caller refreshed while this one waited for the lock.
            return@withLock Tokens(accessToken = current, refreshToken = tokenStore.refreshToken())
        }

        val refreshToken = tokenStore.refreshToken()
        if (refreshToken == null) {
            tokenStore.clear()
            return@withLock null
        }

        val tokens = tokenRefresher.refresh(refreshToken)
        if (tokens == null) {
            // The refresh token is spent. Clearing is what tells the session owner to sign out.
            tokenStore.clear()
            return@withLock null
        }

        tokenStore.save(accessToken = tokens.accessToken, refreshToken = tokens.refreshToken)
        tokens
    }
}
