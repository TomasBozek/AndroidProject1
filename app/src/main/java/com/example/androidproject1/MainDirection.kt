package com.example.androidproject1

sealed interface MainDirection {

    /** The user signed in — swap the auth graph for the main one. */
    data object Main : MainDirection

    /** The user signed out — swap the main graph for the auth one. */
    data object Auth : MainDirection
}
