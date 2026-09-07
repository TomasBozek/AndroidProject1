package com.example.androidproject1.feature.auth.domain

/** An authenticated user. A `null` session means signed out. */
data class Session(
    val email: String,
)
