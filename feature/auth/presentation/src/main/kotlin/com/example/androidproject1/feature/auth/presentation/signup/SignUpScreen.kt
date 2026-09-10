package com.example.androidproject1.feature.auth.presentation.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.AppTextField
import com.example.androidproject1.core.ui.component.ButtonKind
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.auth.presentation.R
import com.example.androidproject1.service.core.ui.text.resolve

@Composable
fun SignUpScreen(
    state: SignUpState,
    onEvent: (SignUpEvent) -> Unit,
) {
    AppScaffold(screenId = "SignUpScreen") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // safeDrawingPadding in AppScaffold shrinks the content area when the keyboard
                // opens, and a large font makes this form taller than a short phone even without
                // one — a form that cannot reach its own submit button is a form nobody can use.
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(
                AppTheme.spacing.stack.md,
                Alignment.CenterVertically,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppText(text = stringResource(R.string.sign_up_title), role = TextRole.Display)
            AppTextField(
                value = state.email.value,
                errorText = state.email.error?.resolve(),
                onValueChange = { onEvent(SignUpEvent.EmailChanged(it)) },
                label = stringResource(R.string.sign_up_email),
                keyboardType = KeyboardType.Email,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signUp_emailField"),
            )
            AppTextField(
                value = state.password.value,
                errorText = state.password.error?.resolve(),
                onValueChange = { onEvent(SignUpEvent.PasswordChanged(it)) },
                label = stringResource(R.string.sign_up_password),
                password = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signUp_passwordField"),
            )
            AppTextField(
                value = state.confirmPassword.value,
                errorText = state.confirmPassword.error?.resolve(),
                onValueChange = { onEvent(SignUpEvent.ConfirmPasswordChanged(it)) },
                label = stringResource(R.string.sign_up_confirm_password),
                password = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signUp_confirmPasswordField"),
            )
            AppButton(
                label = stringResource(R.string.sign_up_submit),
                onClick = { onEvent(SignUpEvent.SignUpClicked) },
                enabled = state.canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signUp_submitButton"),
            )
            AppButton(
                label = stringResource(R.string.sign_up_login),
                onClick = { onEvent(SignUpEvent.LoginClicked) },
                kind = ButtonKind.Ghost,
                modifier = Modifier.testTag("signUp_loginButton"),
            )
        }
    }
}

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    SignUpScreen(
        state = SignUpState.PREVIEW,
    ) {}
}
