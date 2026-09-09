package com.example.androidproject1.feature.profile.presentation

import android.Manifest
import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.androidproject1.core.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * What is on screen, and what a tap does.
 *
 * The half a ViewModel test cannot reach — and here that includes the permission gate, because
 * whether the camera section shows a capture button or a rationale is decided in composition and
 * nowhere else. Robolectric's shadow grants the permission, which is the only way to see both.
 *
 * **Everything is found by `testTag`, never by text.** Copy gets reworded and translated; a test
 * that finds a button by its label fails on a wording change that broke nothing.
 */
private const val ROBOLECTRIC_SDK = 35

@RunWith(RobolectricTestRunner::class)
// Robolectric ships an SDK image per API level and has none for this project's targetSdk, so the
// level is pinned to the newest it does have. Raise it when Robolectric catches up; nothing in a
// screen test depends on the difference.
@Config(sdk = [ROBOLECTRIC_SDK])
class ProfileScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<ProfileEvent>()

    private fun render(state: ProfileState) {
        compose.setContent {
            AppTheme {
                ProfileScreen(state = state, onEvent = events::add)
            }
        }
    }

    private fun grantCamera() {
        shadowOf(ApplicationProvider.getApplicationContext<Application>())
            .grantPermissions(Manifest.permission.CAMERA)
    }

    @Test
    fun `renders the form it is given`() {
        render(ProfileState.PREVIEW)

        compose.onNodeWithTag("ProfileScreen").assertIsDisplayed()
        compose.onNodeWithTag("profile_avatarValue").assertIsDisplayed()
        compose.onNodeWithTag("profile_nameField").assertIsDisplayed()
        compose.onNodeWithTag("profile_emailField").assertIsDisplayed()
        // Scrolled to: the form is taller than a short window, which is what the column's
        // verticalScroll is for. A submit button that cannot be reached is the failure here.
        compose.onNodeWithTag("profile_saveButton").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `renders the empty state without collapsing`() {
        render(ProfileState.EMPTY)

        compose.onNodeWithTag("profile_avatarValue").assertIsDisplayed()
        compose.onNodeWithTag("profile_nameField").assertIsDisplayed()
        compose.onNodeWithTag("profile_saveButton").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `typing a name reports it as an event`() {
        render(ProfileState.EMPTY)

        // AppTextField is a label, an input and a supporting line, and the caller's modifier goes
        // to the group — so a test that types reaches the input inside it.
        compose.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("profile_nameField")))
            .performTextInput("J")

        assertEquals(listOf(ProfileEvent.NameChanged("J")), events)
    }

    @Test
    fun `save stays pressable on an invalid form`() {
        // core.5's point: a disabled button cannot say why. Pressing it is what reveals the errors,
        // so it has to report the tap.
        render(ProfileState.PREVIEW_INVALID)

        compose.onNodeWithTag("profile_saveButton").performScrollTo().performClick()

        assertEquals(listOf(ProfileEvent.SaveClicked), events)
    }

    @Test
    fun `the camera section is closed until it is asked for`() {
        render(ProfileState.PREVIEW)

        compose.onNodeWithTag("profile_takePhotoButton").assertIsDisplayed()
        compose.onNodeWithTag("profile_cameraRationaleTile").assertDoesNotExist()
        compose.onNodeWithTag("profile_captureButton").assertDoesNotExist()
    }

    @Test
    fun `asking for the camera reports it as an event`() {
        render(ProfileState.PREVIEW)

        compose.onNodeWithTag("profile_takePhotoButton").performClick()

        assertEquals(listOf(ProfileEvent.TakePhotoClicked), events)
    }

    @Test
    fun `an ungranted camera shows the rationale instead of the capture button`() {
        render(ProfileState.PREVIEW.copy(cameraOpen = true))

        compose.onNodeWithTag("profile_cameraRationaleTile").assertIsDisplayed()
        compose.onNodeWithTag("profile_cameraPermissionButton").assertIsDisplayed()
        // The gate composes `content` or nothing. There is no ungranted branch to forget.
        compose.onNodeWithTag("profile_captureButton").assertDoesNotExist()
    }

    @Test
    fun `the photo picker is offered whether or not the camera is granted`() {
        // The reason the screen offers both: refusing the camera costs the user nothing.
        render(ProfileState.PREVIEW.copy(cameraOpen = true))

        compose.onNodeWithTag("profile_choosePhotoButton").assertIsDisplayed()
    }

    @Test
    fun `a granted camera shows the capture button and no rationale`() {
        grantCamera()

        render(ProfileState.PREVIEW.copy(cameraOpen = true))

        compose.onNodeWithTag("profile_captureButton").assertIsDisplayed()
        compose.onNodeWithTag("profile_cameraRationaleTile").assertDoesNotExist()
    }
}
