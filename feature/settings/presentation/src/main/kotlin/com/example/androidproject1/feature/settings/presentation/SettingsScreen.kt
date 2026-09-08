package com.example.androidproject1.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.component.ButtonKind
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.theme.AppTheme

@Composable
fun SettingsScreen(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
) {
    AppScaffold(
        // No up arrow: this is a tab root, and the bottom bar is what leaves it.
        topBar = { AppTopBar(title = stringResource(R.string.settings_title)) },
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
        ) {
            AppText(
                text = stringResource(R.string.settings_signed_in_as),
                role = TextRole.LabelSmall,
            )
            AppText(
                text = state.email ?: stringResource(R.string.settings_no_email),
                role = TextRole.BodyLarge,
            )

            AppButton(
                label = stringResource(R.string.settings_permissions),
                onClick = { onEvent(SettingsEvent.PermissionsClicked) },
                kind = ButtonKind.Outline,
                modifier = Modifier.fillMaxWidth(),
            )

            // Signing out is not destructive here — the confirm dialog is what guards it.
            AppButton(
                label = stringResource(R.string.settings_logout),
                onClick = { onEvent(SettingsEvent.LogoutClicked) },
                kind = ButtonKind.Neutral,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    SettingsScreen(
        state = SettingsState.PREVIEW,
    ) {}
}
