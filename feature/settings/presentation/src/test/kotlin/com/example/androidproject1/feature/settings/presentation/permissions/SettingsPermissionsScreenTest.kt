package com.example.androidproject1.feature.settings.presentation.permissions

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.androidproject1.core.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * What is on screen, and what a tap does — the half `SettingsPermissionsViewModelTest` cannot
 * reach, and here that is most of the screen: the platform is read *in composition* and handed to
 * the ViewModel as an event, so nothing but a rendered screen ever produces a `PermissionsRead`.
 *
 * **Everything is found by `testTag`, never by text.**
 */
@RunWith(RobolectricTestRunner::class)
class SettingsPermissionsScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<SettingsPermissionsEvent>()

    /** What the user did, with the screen's own reading of the platform filtered out. */
    private val taps get() = events.filterNot { it is SettingsPermissionsEvent.PermissionsRead }

    private fun render(state: SettingsPermissionsState) {
        compose.setContent {
            AppTheme {
                SettingsPermissionsScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders a row per declared permission`() {
        render(SettingsPermissionsState.PREVIEW)

        compose.onNodeWithTag("SettingsPermissionsScreen").assertIsDisplayed()
        compose.onAllNodesWithTag("settingsPermissions_permissionItem")
            .assertCountEquals(SettingsPermissionsState.PREVIEW.permissions.size)
    }

    @Test
    fun `reads the platform on first composition`() {
        // The mechanism the screen exists to demonstrate: permission state lives outside the app,
        // so it is re-read in composition and handed over as an event rather than observed.
        render(SettingsPermissionsState.PREVIEW)

        assertTrue(events.first() is SettingsPermissionsEvent.PermissionsRead)
    }

    @Test
    fun `the notifications prompt is shown while the app cannot post them`() {
        render(SettingsPermissionsState.PREVIEW)

        compose.onNodeWithTag("settingsPermissions_allowButton").assertIsDisplayed()
    }

    @Test
    fun `the notifications prompt is gone once it can`() {
        render(SettingsPermissionsState.PREVIEW.copy(canPostNotifications = true))

        compose.onNodeWithTag("settingsPermissions_allowButton").assertDoesNotExist()
    }

    @Test
    fun `opening the app settings reports it as an event`() {
        // The only way through once the system dialog has stopped appearing, which is why it is
        // below the list rather than inside the prompt.
        render(SettingsPermissionsState.PREVIEW)

        compose.onNodeWithTag("settingsPermissions_openSettingsButton")
            .performScrollTo()
            .performClick()

        assertEquals(listOf(SettingsPermissionsEvent.OpenAppSettingsClicked), taps)
    }
}
