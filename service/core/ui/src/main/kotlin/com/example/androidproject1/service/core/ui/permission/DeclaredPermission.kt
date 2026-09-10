package com.example.androidproject1.service.core.ui.permission

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect

/** A permission this app's manifest asks for, and whether it is held. */
@Immutable
data class DeclaredPermission(
    /** The full name, e.g. `android.permission.POST_NOTIFICATIONS`. */
    val name: String,
    val isGranted: Boolean,
)

/**
 * Every permission the merged manifest declares, with its current state.
 *
 * Read from `PackageManager` rather than listed by hand, so a permission added by a library shows
 * up too — which is the only way this is worth having. Re-read on resume, because the user can
 * change any of them in system settings while the app is in the background.
 */
@Composable
fun rememberDeclaredPermissions(): List<DeclaredPermission> {
    val context = LocalContext.current
    var revision by remember { mutableIntStateOf(0) }

    LifecycleResumeEffect(Unit) {
        revision++
        onPauseOrDispose {}
    }

    return remember(revision) { context.declaredPermissions() }
}

private fun Context.declaredPermissions(): List<DeclaredPermission> {
    // The typed-flags overload is API 33; minSdk is 29, so both spellings are needed until it is not.
    val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val flags = PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
        packageManager.getPackageInfo(packageName, flags)
    } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
    }

    return info.requestedPermissions.orEmpty()
        .sorted()
        .map { DeclaredPermission(name = it, isGranted = isGranted(it)) }
}
