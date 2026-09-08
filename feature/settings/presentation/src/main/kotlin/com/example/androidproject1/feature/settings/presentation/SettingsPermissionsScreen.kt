package com.example.androidproject1.feature.settings.presentation

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.permission.PermissionStatus
import com.example.androidproject1.core.ui.permission.rememberDeclaredPermissions
import com.example.androidproject1.core.ui.permission.rememberPermissionRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPermissionsScreen(
    state: SettingsPermissionsState,
    onEvent: (SettingsPermissionsEvent) -> Unit,
) {
    // Read here rather than in the ViewModel, and handed over as an event: a permission changes
    // outside the app, so re-reading on resume is the whole mechanism. See SettingsPermissionsEvent.
    val declared = rememberDeclaredPermissions()
    LaunchedEffect(declared) {
        onEvent(SettingsPermissionsEvent.PermissionsRead(declared))
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings_permissions_title)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(SettingsPermissionsEvent.NavigateUpClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_permissions_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (!state.canPostNotifications) {
                item { NotificationsPrompt(modifier = Modifier.padding(16.dp)) }
            }

            items(state.permissions, key = { it.name }) { row ->
                PermissionListItem(row = row)
                HorizontalDivider()
            }

            item {
                OutlinedButton(
                    onClick = { onEvent(SettingsPermissionsEvent.OpenAppSettingsClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(text = stringResource(R.string.settings_permissions_open_settings))
                }
            }
        }
    }
}

/**
 * The one permission this app asks for itself, asked for where the user came to look at
 * permissions — rather than thrown at them on first launch, which is what gets it denied.
 */
@Composable
private fun NotificationsPrompt(modifier: Modifier = Modifier) {
    val request = rememberPermissionRequest(Manifest.permission.POST_NOTIFICATIONS)
    val status = request.status
    val canAskAgain = status !is PermissionStatus.Denied || status.canAskAgain

    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_permissions_notifications_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.settings_permissions_notifications_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // Once the user has denied twice the system dialog never appears again, so offering
            // "Allow" would do nothing at all. The button below the list is the way through.
            if (canAskAgain) {
                TextButton(onClick = request::request) {
                    Text(text = stringResource(R.string.settings_permissions_allow))
                }
            }
        }
    }
}

@Composable
private fun PermissionListItem(row: PermissionRow, modifier: Modifier = Modifier) {
    ListItem(
        modifier = modifier,
        headlineContent = { Text(text = row.label) },
        supportingContent = {
            Text(text = row.name, style = MaterialTheme.typography.bodySmall)
        },
        trailingContent = {
            val label = if (row.isGranted) {
                R.string.settings_permissions_granted
            } else {
                R.string.settings_permissions_denied
            }
            SuggestionChip(
                onClick = {},
                enabled = false,
                label = { Text(text = stringResource(label)) },
            )
        },
    )
}

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    SettingsPermissionsScreen(
        state = SettingsPermissionsState.PREVIEW,
    ) {}
}
