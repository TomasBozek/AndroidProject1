package com.example.androidproject1.service.core.ui.permission

import android.Manifest
import android.provider.Settings
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.androidproject1.service.core.ui.text.UiText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

private const val CONTENT_TAG = "gate_content"

/**
 * The gate composes its content only while the permission is held, and the rationale's one button
 * does the only thing that can still move each case on.
 *
 * The gate is asserted by what is on screen rather than by the status it read: the whole point of
 * the type is that an ungranted case cannot be forgotten, and only composition can show that.
 */
@RunWith(RobolectricTestRunner::class)
class PermissionGateTest {

    @get:Rule
    val compose = createComposeRule()

    private val application = RuntimeEnvironment.getApplication()

    private val camera = Manifest.permission.CAMERA

    private val rationale = permissionRationale(
        title = UiText.Literal("Camera"),
        message = UiText.Literal("Needed to take a photo."),
    )

    @Test
    fun `a held permission composes the content`() {
        shadowOf(application).grantPermissions(camera)

        compose.setContent {
            PermissionGate(camera, rationale = rationale) {
                Text("photo", modifier = Modifier.testTag(CONTENT_TAG))
            }
        }

        compose.onNodeWithTag(CONTENT_TAG).assertIsDisplayed()
    }

    @Test
    fun `an ungranted permission composes the rationale and not the content`() {
        shadowOf(application).denyPermissions(camera)

        compose.setContent {
            PermissionGate(camera, rationale = rationale) {
                Text("photo", modifier = Modifier.testTag(CONTENT_TAG))
            }
        }

        // Not merely hidden: `content` never runs, which is what makes a forgotten ungranted case
        // impossible rather than unlikely.
        compose.onNodeWithTag(CONTENT_TAG).assertDoesNotExist()
        compose.onNodeWithTag(PERMISSION_ACTION_TAG).assertIsDisplayed()
    }

    @Test
    fun `while it can still be asked for, the button asks`() {
        var asked = false
        val request = PermissionRequest(
            permissions = listOf(camera),
            status = PermissionStatus.Denied(canAskAgain = true),
            onRequest = { asked = true },
        )

        compose.setContent { PermissionRationaleContent(rationale = rationale, request = request) }
        compose.onNodeWithTag(PERMISSION_ACTION_TAG).performClick()

        assertEquals(true, asked)
        assertEquals(null, shadowOf(application).nextStartedActivity)
    }

    @Test
    fun `once it cannot, the button opens system settings instead`() {
        var asked = false
        val request = PermissionRequest(
            permissions = listOf(camera),
            status = PermissionStatus.Denied(canAskAgain = false),
            onRequest = { asked = true },
        )

        compose.setContent { PermissionRationaleContent(rationale = rationale, request = request) }
        compose.onNodeWithTag(PERMISSION_ACTION_TAG).performClick()

        // Asking again would show nothing at all — the platform stops raising the dialog — so the
        // button has to lead somewhere else or the user is stuck on this screen.
        assertEquals(false, asked)
        val started = shadowOf(application).nextStartedActivity
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, started?.action)
        assertTrue(started?.data.toString().endsWith(application.packageName))
    }
}
