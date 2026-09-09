package com.example.androidproject1.core.network

/**
 * What a client needs that is not a dependency: the host it talks to and how long it waits.
 *
 * A data class rather than parameters, so adding a knob does not change every call site — and so
 * a flavor can build one from `BuildConfig` in a single line.
 */
data class NetworkConfig(
    val baseUrl: String,
    val requestTimeoutMillis: Long = 30_000,
    val connectTimeoutMillis: Long = 15_000,
    val socketTimeoutMillis: Long = 30_000,
    /** Bodies and headers in the log. Never on a release build: tokens travel in both. */
    val logBodies: Boolean = false,
)
