package com.example.androidproject1.feature.auth.presentation

data class SignUpState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
) {

    val canSubmit: Boolean get() =
        email.isNotBlank() && password.isNotBlank() && password == confirmPassword

    companion object {

        val PREVIEW = SignUpState(
            email = "ada@example.com",
            password = "hunter2",
            confirmPassword = "hunter2",
        )
    }
}
