package com.example.androidproject1.feature.auth.presentation.signup

import androidx.compose.runtime.Immutable
import com.example.androidproject1.core.ui.form.FieldState
import com.example.androidproject1.core.ui.form.Form
import com.example.androidproject1.core.ui.form.email
import com.example.androidproject1.core.ui.form.minLength
import com.example.androidproject1.core.ui.form.required

@Immutable
data class SignUpState(
    val email: FieldState = FieldState.of(required(), email()),
    val password: FieldState = FieldState.of(required(), minLength(MIN_PASSWORD_LENGTH)),
    val confirmPassword: FieldState = FieldState.of(required()),
) {

    // The confirm rule needs the other field, so it is checked here rather than as a validator on
    // confirmPassword: a validator sees one value, and this one is about two.
    val passwordsMatch: Boolean get() = password.value == confirmPassword.value

    val canSubmit: Boolean get() =
        Form.canSubmit(email, password, confirmPassword) && passwordsMatch

    companion object {

        const val MIN_PASSWORD_LENGTH = 8

        val PREVIEW = SignUpState(
            email = FieldState.of(required(), email(), value = "ada@example.com"),
            password = FieldState.of(required(), minLength(MIN_PASSWORD_LENGTH), value = "hunter2!!"),
            confirmPassword = FieldState.of(required(), value = "hunter2!!"),
        )
    }
}
