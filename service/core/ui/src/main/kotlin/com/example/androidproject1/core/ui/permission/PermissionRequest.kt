package com.example.androidproject1.core.ui.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect

/**
 * One permission request, and what the screen may do about it.
 *
 * Immutable: a new one is produced whenever [status] changes, so a screen can compare it and
 * nothing has to be observed.
 */
@Immutable
class PermissionRequest internal constructor(
    val permissions: List<String>,
    val status: PermissionStatus,
    private val onRequest: () -> Unit,
) {

    /**
     * Shows the system dialog, or does nothing visible once the user has denied permanently — that
     * is the platform's behaviour, and [PermissionStatus.Denied.canAskAgain] is how to know in
     * advance. Send the user to [openAppSettings] instead.
     */
    fun request() = onRequest()
}

/**
 * Remembers a [PermissionRequest] for [permissions], reflecting what is already held.
 *
 * ```
 * val notifications = rememberPermissionRequest(Manifest.permission.POST_NOTIFICATIONS)
 * Button(onClick = notifications::request) { Text("Enable notifications") }
 * ```
 *
 * Wraps `rememberLauncherForActivityResult` rather than pulling in a permissions library: the
 * contract is two calls, and what is actually worth having — the four-case status, `canAskAgain`,
 * and re-reading on resume — is small enough to own.
 */
@Composable
fun rememberPermissionRequest(vararg permissions: String): PermissionRequest {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val names = remember(permissions) { permissions.toList() }

    // shouldShowRequestPermissionRationale() is false both before the first request and after a
    // permanent denial, so "have we asked?" has to be remembered separately — and across process
    // death, or a permanent denial reads as NotRequested once the app has been killed.
    var hasRequested by rememberSaveable { mutableStateOf(false) }

    // Bumped whenever the answer might have changed: a result arrived, or the screen resumed. The
    // permission itself lives outside the app, so there is nothing to observe — only to re-read.
    var revision by remember { mutableIntStateOf(0) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        hasRequested = true
        revision++
    }

    // The user can grant or revoke in system settings and come back to an app that never asks again.
    LifecycleResumeEffect(Unit) {
        revision++
        onPauseOrDispose {}
    }

    val status = remember(names, hasRequested, revision) {
        context.permissionStatus(
            permissions = names,
            hasRequested = hasRequested,
            canAskAgain = { activity?.shouldShowRequestPermissionRationale(it) == true },
        )
    }

    return remember(names, status) {
        PermissionRequest(
            permissions = names,
            status = status,
            onRequest = { launcher.launch(names.toTypedArray()) },
        )
    }
}

/**
 * The status of [permissions] taken together.
 *
 * @param hasRequested distinguishes "not asked yet" from "denied", which the platform APIs alone
 * cannot: `shouldShowRequestPermissionRationale` is false in both cases.
 */
internal fun Context.permissionStatus(
    permissions: List<String>,
    hasRequested: Boolean,
    canAskAgain: (String) -> Boolean,
): PermissionStatus {
    if (permissions.isEmpty()) return PermissionStatus.Granted

    val (granted, denied) = permissions.partition { isGranted(it) }
    return when {
        denied.isEmpty() -> PermissionStatus.Granted
        granted.isNotEmpty() -> PermissionStatus.PartiallyGranted(granted.toSet(), denied.toSet())
        !hasRequested -> PermissionStatus.NotRequested
        else -> PermissionStatus.Denied(canAskAgain = denied.any(canAskAgain))
    }
}

internal fun Context.isGranted(permission: String): Boolean =
    checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
