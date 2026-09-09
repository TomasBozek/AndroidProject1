package com.example.androidproject1.core.ui.permission

import android.Manifest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

private const val ROBOLECTRIC_SDK = 35

/**
 * The four statuses, against a real `PackageManager` with the grants shadowed.
 *
 * This is the state machine the permission UI branches on, and the branch that costs the most to
 * get wrong is invisible from the app: once `canAskAgain` is false the system dialog never appears
 * again, so a screen that keeps calling `request()` strands the user with a button that does
 * nothing. Each case is asserted rather than the boolean it used to be.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ROBOLECTRIC_SDK])
class PermissionStatusTest {

    private val application = RuntimeEnvironment.getApplication()

    private val camera = Manifest.permission.CAMERA
    private val notifications = Manifest.permission.POST_NOTIFICATIONS

    private fun grant(vararg permissions: String) = shadowOf(application).grantPermissions(*permissions)

    private fun deny(vararg permissions: String) = shadowOf(application).denyPermissions(*permissions)

    private fun statusOf(
        vararg permissions: String,
        hasRequested: Boolean = true,
        canAskAgain: Boolean = true,
    ) = application.permissionStatus(
        permissions = permissions.toList(),
        hasRequested = hasRequested,
        canAskAgain = { canAskAgain },
    )

    @Test
    fun `every permission held is granted`() {
        grant(camera, notifications)

        assertEquals(PermissionStatus.Granted, statusOf(camera, notifications))
    }

    @Test
    fun `asking for nothing is granted, so a gate over an empty list composes`() {
        // An empty list reaches here from a `vararg` a caller built at runtime; treating it as
        // denied would hide the content behind a rationale about no permission at all.
        assertEquals(PermissionStatus.Granted, statusOf())
    }

    @Test
    fun `one held and one not is partially granted, and names both sides`() {
        grant(camera)
        deny(notifications)

        assertEquals(
            PermissionStatus.PartiallyGranted(granted = setOf(camera), denied = setOf(notifications)),
            statusOf(camera, notifications),
        )
    }

    @Test
    fun `a partial grant is not Granted`() {
        grant(camera)
        deny(notifications)

        // The reason PartiallyGranted is not a subtype: `is Granted` would be true for it, and
        // the caller that only checks that would run its content with half of what it asked for.
        assertEquals(false, statusOf(camera, notifications) is PermissionStatus.Granted)
    }

    @Test
    fun `not held and never asked is not requested`() {
        deny(camera)

        assertEquals(PermissionStatus.NotRequested, statusOf(camera, hasRequested = false))
    }

    @Test
    fun `not held after asking is denied, and can be asked again`() {
        deny(camera)

        assertEquals(PermissionStatus.Denied(canAskAgain = true), statusOf(camera, canAskAgain = true))
    }

    @Test
    fun `a permanent denial says so, because only system settings can undo it`() {
        deny(camera)

        assertEquals(PermissionStatus.Denied(canAskAgain = false), statusOf(camera, canAskAgain = false))
    }

    @Test
    fun `one of several still askable makes the whole request askable`() {
        deny(camera, notifications)

        val status = application.permissionStatus(
            permissions = listOf(camera, notifications),
            hasRequested = true,
            // The platform answers per permission, and a dialog that can still be shown for one of
            // them is a dialog: sending the user to settings instead would skip a question they
            // would have answered.
            canAskAgain = { it == notifications },
        )

        assertEquals(PermissionStatus.Denied(canAskAgain = true), status)
    }
}
