package com.example.androidproject1.feature.auth.presentation.signup

/**
 * Signing up changes the session, same as signing in — MainViewModel reacts and switches the
 * graph. [Login] is the only real intent here: back to the sign-in screen.
 */
sealed interface SignUpNavigation {

    data object Login : SignUpNavigation
}
