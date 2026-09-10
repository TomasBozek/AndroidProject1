package com.example.androidproject1.feature.settings.presentation.component

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppCard
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.ButtonKind
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.feature.settings.presentation.R
import com.example.androidproject1.service.core.ui.permission.PermissionStatus
import com.example.androidproject1.service.core.ui.permission.rememberPermissionRequest

/**
 * The one permission this app asks for itself, asked for where the user came to look at
 * permissions — rather than thrown at them on first launch, which is what gets it denied.
 */
@Composable
fun NotificationsPrompt(modifier: Modifier = Modifier) {
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
                modifier = Modifier.testTag("settingsPermissions_allowButton"),
            )
        }
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    NotificationsPrompt()
}
