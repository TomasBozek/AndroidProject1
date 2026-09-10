package com.example.androidproject1.feature.settings.presentation.permissions

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppDivider
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.component.ButtonKind
import com.example.androidproject1.core.ui.permission.rememberDeclaredPermissions
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.settings.presentation.R
import com.example.androidproject1.feature.settings.presentation.component.NotificationsPrompt
import com.example.androidproject1.feature.settings.presentation.component.PermissionListItem

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
        screenId = "SettingsPermissionsScreen",
        topBar = {
            AppTopBar(
                title = stringResource(R.string.settings_permissions_title),
                onNavigateUp = { onEvent(SettingsPermissionsEvent.NavigateUpClicked) },
                navigateUpTestTag = "settingsPermissions_upButton",
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
                        .padding(AppTheme.spacing.inset.lg)
                        .testTag("settingsPermissions_openSettingsButton"),
                )
            }
        }
    }
}

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    SettingsPermissionsScreen(
        state = SettingsPermissionsState.PREVIEW,
    ) {}
}
