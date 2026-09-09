package com.example.androidproject1.feature.profile.presentation.profile

/** One-off navigation intents, turned into back-stack calls in ProfileDestination. */
sealed interface ProfileNavigation {

    data object NavigateUp : ProfileNavigation
}
