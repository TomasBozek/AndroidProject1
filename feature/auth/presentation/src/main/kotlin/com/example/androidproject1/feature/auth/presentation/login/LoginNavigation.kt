package com.example.androidproject1.feature.auth.presentation.login

/**
 * Signing in itself changes the session, and MainViewModel switches the graph from there —
 * [SignUp] is the only real intent this screen has left: moving to the sign-up screen.
 */
sealed interface LoginNavigation {

    data object SignUp : LoginNavigation
}
