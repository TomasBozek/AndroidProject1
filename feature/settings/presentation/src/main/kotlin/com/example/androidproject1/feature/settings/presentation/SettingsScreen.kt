package com.example.androidproject1.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.ButtonKind
import com.example.androidproject1.core.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // No up arrow: this is a tab root, and the bottom bar is what leaves it.
            TopAppBar(title = { Text(text = stringResource(R.string.settings_title)) })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(AppTheme.spacing.inset.xl),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
        ) {
            Text(
                text = stringResource(R.string.settings_signed_in_as),
                style = AppTheme.typography.labelSm,
                color = AppTheme.colors.textSecondary,
            )
            Text(
                text = state.email ?: stringResource(R.string.settings_no_email),
                style = AppTheme.typography.bodyLg,
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
