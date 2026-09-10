package com.example.androidproject1.core.ui.permission

import android.Manifest
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

/**
 * What the merged manifest asks for, read back through `PackageManager`.
 *
 * Reading the manifest rather than keeping a list is the only thing that makes the permissions
 * screen worth having: a permission a library contributed appears there too, and nobody has to
 * remember to add it.
 */
@RunWith(RobolectricTestRunner::class)
class DeclaredPermissionsTest {

    @get:Rule
    val compose = createComposeRule()

    private val application = RuntimeEnvironment.getApplication()

    private val camera = Manifest.permission.CAMERA
    private val notifications = Manifest.permission.POST_NOTIFICATIONS

    private fun declaring(vararg permissions: String) {
        shadowOf(application.packageManager)
            .getInternalMutablePackageInfo(application.packageName)
            .requestedPermissions = arrayOf(*permissions)
    }

    private fun read(): List<DeclaredPermission> {
        var declared: List<DeclaredPermission>? = null
        compose.setContent { declared = rememberDeclaredPermissions() }
        compose.waitForIdle()
        return requireNotNull(declared) { "rememberDeclaredPermissions never composed" }
    }

    @Test
    fun `every declared permission is listed, sorted, with whether it is held`() {
        declaring(notifications, camera)
        shadowOf(application).grantPermissions(camera)
        shadowOf(application).denyPermissions(notifications)

        val declared = read()

        // Sorted, so the screen does not reorder itself when the manifest merge order changes.
        assertEquals(
            listOf(
                DeclaredPermission(name = camera, isGranted = true),
                DeclaredPermission(name = notifications, isGranted = false),
            ),
            declared,
        )
    }

    @Test
    fun `a manifest that asks for nothing lists nothing`() {
        declaring()

        assertEquals(emptyList<DeclaredPermission>(), read())
    }
}
