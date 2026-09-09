package com.example.androidproject1.feature.settings.presentation.permissions

import androidx.compose.runtime.Immutable

/**
 * One permission the manifest declares.
 *
 * @property label the last segment of [name] — `POST_NOTIFICATIONS` rather than the full
 * `android.permission.POST_NOTIFICATIONS`, which is what a person reads.
 */
@Immutable
data class PermissionRow(
    val name: String,
    val label: String,
    val isGranted: Boolean,
)

@Immutable
data class SettingsPermissionsState(
    val permissions: List<PermissionRow>,
    /** Whether this app can post notifications, the one permission it asks for itself. */
    val canPostNotifications: Boolean,
) {

    companion object {

        val PREVIEW = SettingsPermissionsState(
            permissions = listOf(
                PermissionRow(
                    name = "android.permission.INTERNET",
                    label = "INTERNET",
                    isGranted = true,
                ),
                PermissionRow(
                    name = "android.permission.POST_NOTIFICATIONS",
                    label = "POST_NOTIFICATIONS",
                    isGranted = false,
                ),
            ),
            canPostNotifications = false,
        )
    }
}
