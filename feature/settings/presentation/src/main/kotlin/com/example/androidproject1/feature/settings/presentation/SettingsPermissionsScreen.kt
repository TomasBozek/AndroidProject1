package com.example.androidproject1.feature.settings.presentation

import android.Manifest
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppCard
import com.example.androidproject1.core.ui.component.AppDivider
import com.example.androidproject1.core.ui.component.AppListItem
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppTag
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.component.ButtonKind
import com.example.androidproject1.core.ui.component.TagTone
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.permission.PermissionStatus
import com.example.androidproject1.core.ui.permission.rememberDeclaredPermissions
import com.example.androidproject1.core.ui.permission.rememberPermissionRequest
import com.example.androidproject1.core.ui.theme.AppTheme

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
    AppScaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.settings_permissions_title),
                onNavigateUp = { onEvent(SettingsPermissionsEvent.NavigateUpClicked) },
            )
        },
        contentPadding = false,
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (!state.canPostNotifications) {
                item {
                    NotificationsPrompt(
                        modifier = Modifier.padding(AppTheme.spacing.inset.lg),
                    )
                }
            }
            items(state.permissions, key = { it.name }) { row ->
                PermissionListItem(row = row)
                AppDivider()
            }
            item {
                AppButton(
                    label = stringResource(R.string.settings_permissions_open_settings),
                    onClick = { onEvent(SettingsPermissionsEvent.OpenAppSettingsClicked) },
                    kind = ButtonKind.Outline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppTheme.spacing.inset.lg),
                )
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
    AppCard(modifier = modifier) {
        AppText(
            text = stringResource(R.string.settings_permissions_notifications_title),
            role = TextRole.Title,
        )
        AppText(
            text = stringResource(R.string.settings_permissions_notifications_message),
            role = TextRole.Secondary,
        )
        // Once the user has denied twice the system dialog never appears again, so offering
        // "Allow" would do nothing at all. The button below the list is the way through.
        if (canAskAgain) {
            AppButton(
                label = stringResource(R.string.settings_permissions_allow),
                onClick = request::request,
                kind = ButtonKind.Ghost,
            )
        }
    }
}

@Composable
private fun PermissionListItem(row: PermissionRow, modifier: Modifier = Modifier) {
    AppListItem(
        modifier = modifier,
        headline = row.label,
        supporting = row.name,
        trailing = {
            AppTag(
                label = if (row.isGranted) {
                    stringResource(R.string.settings_permissions_granted)
                } else {
                    stringResource(R.string.settings_permissions_denied)
                },
                tone = if (row.isGranted) TagTone.Paid else TagTone.Void,
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
