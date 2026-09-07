package com.example.androidproject1.feature.auth.presentation

/**
 * Intentionally empty.
 *
 * Signing in does not navigate from here: `MainViewModel` observes the session and is the single
 * place that decides which nav graph is current. That keeps "am I signed in" and "which graph am
 * I on" from being two sources of truth that can disagree.
 */
sealed interface LoginDirection
