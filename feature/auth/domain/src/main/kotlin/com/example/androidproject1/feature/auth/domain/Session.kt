package com.example.androidproject1.feature.auth.domain

/**
 * An authenticated user. A `null` session means signed out.
 *
 * [id] is opaque and is the only part that may leave the app: it is what `ErrorTracker.setUser` is
 * given, so a crash report can be tied to a sequence of events without carrying an address.
 * [email] stays inside.
 */
data class Session(
    val id: String,
    val email: String,
)
