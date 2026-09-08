package com.example.androidproject1.core.ui.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * Opens this app's page in system settings — where a permanently denied permission can be granted.
 *
 * One implementation, used by both `Screen()`'s [com.example.androidproject1.core.ui.event
 * .UiCommand.OpenAppSettings] and the permission UI below, so the two cannot drift.
 */
fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            // The Context here may not be an Activity, and a non-Activity Context cannot start one
            // without its own task.
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
    )
}
