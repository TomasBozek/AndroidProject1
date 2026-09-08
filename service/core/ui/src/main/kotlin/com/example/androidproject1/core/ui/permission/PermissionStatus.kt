package com.example.androidproject1.core.ui.permission

/**
 * Where a runtime permission request stands.
 *
 * Modelled as four cases rather than a boolean because the three "not granted" ones need different
 * UI: [NotRequested] asks, [Denied] with `canAskAgain` asks again, and [Denied] without it can only
 * send the user to system settings.
 */
sealed interface PermissionStatus {

    /** Nothing has been asked for yet, and nothing was already held. */
    data object NotRequested : PermissionStatus

    /** Every permission asked for is held. */
    data object Granted : PermissionStatus

    /**
     * Some of them are, some are not — only a multi-permission request can land here.
     *
     * Deliberately **not** a subtype of [Granted]: `status is Granted` would then be true for a
     * partial grant, which is the footgun this type exists to remove. A caller that is happy with
     * a subset says so by naming this case.
     */
    data class PartiallyGranted(
        val granted: Set<String>,
        val denied: Set<String>,
    ) : PermissionStatus

    /**
     * @property canAskAgain false once the user has denied permanently, or when the system will not
     * show the dialog again. The only way forward then is app settings.
     */
    data class Denied(val canAskAgain: Boolean) : PermissionStatus
}
