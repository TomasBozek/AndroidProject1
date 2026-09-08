package com.example.androidproject1.feature.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun SignUpScreen(
    state: SignUpState,
    onEvent: (SignUpEvent) -> Unit,
) {
    AppScaffold {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(
                AppTheme.spacing.stack.md,
                Alignment.CenterVertically,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppText(text = stringResource(R.string.sign_up_title), role = TextRole.Display)
            AppTextField(
                value = state.email,
                onValueChange = { onEvent(SignUpEvent.EmailChanged(it)) },
                label = stringResource(R.string.sign_up_email),
                keyboardType = KeyboardType.Email,
                modifier = Modifier.fillMaxWidth(),
            )
            AppTextField(
                value = state.password,
                onValueChange = { onEvent(SignUpEvent.PasswordChanged(it)) },
                label = stringResource(R.string.sign_up_password),
                password = true,
                modifier = Modifier.fillMaxWidth(),
            )
            AppTextField(
                value = state.confirmPassword,
                onValueChange = { onEvent(SignUpEvent.ConfirmPasswordChanged(it)) },
                label = stringResource(R.string.sign_up_confirm_password),
                password = true,
                modifier = Modifier.fillMaxWidth(),
            )
            AppButton(
                label = stringResource(R.string.sign_up_submit),
                onClick = { onEvent(SignUpEvent.SignUpClicked) },
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth(),
            )
            AppButton(
                label = stringResource(R.string.sign_up_login),
                onClick = { onEvent(SignUpEvent.LoginClicked) },
                kind = ButtonKind.Ghost,
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
