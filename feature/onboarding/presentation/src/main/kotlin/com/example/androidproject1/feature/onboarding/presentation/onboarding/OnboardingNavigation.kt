package com.example.androidproject1.feature.onboarding.presentation.onboarding

/**
 * One-off navigation intents, turned into back-stack calls in OnboardingDestination.
 *
 * There are none, and that is the point of this flow: finishing the tour writes the stored flag,
 * and `MainViewModel` reacts to it by putting the auth flow on the back stack. A screen changes
 * what the app knows and lets the root switch flows — it never replaces the back stack itself.
 */
sealed interface OnboardingNavigation
