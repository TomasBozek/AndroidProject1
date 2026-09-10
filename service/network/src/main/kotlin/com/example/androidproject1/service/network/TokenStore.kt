package com.example.androidproject1.service.network

/**
 * Where the client gets its tokens and where a refreshed pair goes back to.
 *
 * An interface, and in this module rather than in a feature, because `:service:network` must not
 * know how this app stores a session — `feat.6`'s encrypted DataStore, someone else's Keychain.
 * What it knows is that a token can be read, refreshed and cleared.
 */
interface TokenStore {

    suspend fun accessToken(): String?

    suspend fun refreshToken(): String?

    suspend fun save(accessToken: String, refreshToken: String?)

    /** Called when a refresh fails: the session is over and something above has to react. */
    suspend fun clear()
}
