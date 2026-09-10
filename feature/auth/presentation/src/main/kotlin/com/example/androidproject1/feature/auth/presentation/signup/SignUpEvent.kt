package com.example.androidproject1.feature.auth.presentation.signup

import com.example.androidproject1.service.core.ui.event.UiEvent

sealed interface SignUpEvent : UiEvent {

    data class EmailChanged(val email: String) : SignUpEvent

    data class PasswordChanged(val password: String) : SignUpEvent

    data class ConfirmPasswordChanged(val confirmPassword: String) : SignUpEvent

    data object SignUpClicked : SignUpEvent

    data object LoginClicked : SignUpEvent
}
