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
    /**
     * Retries after the first attempt, so the default is up to three tries in all. `0` installs no
     * retry plugin at all — one request, one answer, which is what a test that counts requests
     * wants and what a flavor talking to a fixture engine needs.
     *
     * Only idempotent methods are ever retried; see [HttpClientFactory].
     */
    val retries: Int = 2,
)
