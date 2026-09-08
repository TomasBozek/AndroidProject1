package com.example.androidproject1.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview

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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_signed_in_as),
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = state.email ?: stringResource(R.string.settings_no_email),
                style = MaterialTheme.typography.bodyLarge,
            )

            OutlinedButton(
                onClick = { onEvent(SettingsEvent.LogoutClicked) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.settings_logout))
            }
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
