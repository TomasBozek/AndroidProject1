package com.example.androidproject1.feature.auth.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.androidproject1.core.ui.text.resolve
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.auth.presentation.R

@Composable
fun LoginScreen(
    state: LoginState,
    onEvent: (LoginEvent) -> Unit,
) {
    AppScaffold(screenId = "LoginScreen") {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(
                AppTheme.spacing.stack.md,
                Alignment.CenterVertically,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppText(text = stringResource(R.string.login_title), role = TextRole.Display)
            AppTextField(
                value = state.email.value,
                errorText = state.email.error?.resolve(),
                onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
                label = stringResource(R.string.login_email),
                keyboardType = KeyboardType.Email,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_emailField"),
            )
            AppTextField(
                value = state.password.value,
                errorText = state.password.error?.resolve(),
                onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
                label = stringResource(R.string.login_password),
                password = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_passwordField"),
            )
            AppButton(
                label = stringResource(R.string.login_submit),
                onClick = { onEvent(LoginEvent.LoginClicked) },
                enabled = state.canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_submitButton"),
            )
            AppButton(
                label = stringResource(R.string.login_skip),
                onClick = { onEvent(LoginEvent.SkipLoginClicked) },
                kind = ButtonKind.Ghost,
            )
            AppButton(
                label = stringResource(R.string.login_sign_up),
                onClick = { onEvent(LoginEvent.SignUpClicked) },
                kind = ButtonKind.Ghost,
                modifier = Modifier.testTag("login_signUpButton"),
            )
        }
    }
}

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    LoginScreen(
        state = LoginState.PREVIEW,
    ) {}
}

/** The error text under each field — the state a design review has to be able to see. */
@ScreenPreview
@Composable
private fun InvalidPreview() = ThemedScreenPreview {
    LoginScreen(
        state = LoginState.PREVIEW_INVALID,
    ) {}
}
