package com.example.androidproject1.feature.auth.presentation

data class LoginState(
    val email: String = "",
    val password: String = "",
) {

    val canSubmit: Boolean get() = email.isNotBlank() && password.isNotBlank()

    companion object {

        val PREVIEW = LoginState(
            email = "ada@example.com",
            password = "hunter2",
        )
    }
}
