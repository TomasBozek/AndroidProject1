package com.example.androidproject1.feature.auth.presentation

import androidx.compose.runtime.Immutable
import com.example.androidproject1.core.ui.form.FieldState
import com.example.androidproject1.core.ui.form.Form
import com.example.androidproject1.core.ui.form.email
import com.example.androidproject1.core.ui.form.required

@Immutable
data class LoginState(
    val email: FieldState = FieldState.of(required(), email()),
    val password: FieldState = FieldState.of(required()),
) {

    // Derived from the fields rather than hand-rolled, so "why is submit disabled" and "is submit
    // disabled" cannot disagree — and the reason is already on the field, ready to show.
    val canSubmit: Boolean get() = Form.canSubmit(email, password)

    companion object {

        val PREVIEW = LoginState(
            email = FieldState.of(required(), email(), value = "ada@example.com"),
            password = FieldState.of(required(), value = "hunter2"),
        )

        /** Nothing typed: submit disabled, and no errors yet because nothing is touched. */
        val EMPTY = LoginState()

        /** What the screen looks like once someone has cleared a field they had filled in. */
        val PREVIEW_INVALID = LoginState(
            email = FieldState.of(required(), email(), value = "not-an-address").changed("not-an-address"),
            password = FieldState.of(required(), value = "").changed(""),
        )
    }
}
