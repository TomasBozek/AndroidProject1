package com.example.androidproject1.feature.auth.presentation

import com.example.androidproject1.core.ui.Event

sealed interface LoginEvent : Event {

    data class EmailChanged(val email: String) : LoginEvent

    data class PasswordChanged(val password: String) : LoginEvent

    data object LoginClicked : LoginEvent

    data object SkipLoginClicked : LoginEvent
}
