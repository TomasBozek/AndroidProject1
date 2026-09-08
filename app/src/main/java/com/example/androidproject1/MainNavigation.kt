package com.example.androidproject1

sealed interface MainNavigation {

    /** Signed in: swap the auth graph for the main one. */
    data object Main : MainNavigation

    /** Signed out: swap the main graph for the auth one. */
    data object Auth : MainNavigation
}
