package com.example.androidproject1.feature.settings.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.androidproject1.core.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Robolectric ships an SDK image per API level and has none for this project's targetSdk, so the
// level is pinned to the newest it does have.
private const val ROBOLECTRIC_SDK = 35

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ROBOLECTRIC_SDK])
class SettingsScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<SettingsEvent>()

    private fun render(state: SettingsState) {
        compose.setContent {
            AppTheme {
                SettingsScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders the session and the entries`() {
        render(SettingsState.PREVIEW)

        compose.onNodeWithTag("settings_emailValue").assertIsDisplayed()
        compose.onNodeWithTag("settings_profileButton").assertIsDisplayed()
        compose.onNodeWithTag("settings_permissionsButton").assertIsDisplayed()
        compose.onNodeWithTag("settings_logoutButton").assertIsDisplayed()
    }

    @Test
    fun `a build with a debug menu offers the entry`() {
        render(SettingsState.PREVIEW.copy(debugMenuEnabled = true))

        compose.onNodeWithTag("settings_debugMenuButton").assertIsDisplayed()
    }

    @Test
    fun `a build without one does not`() {
        // What a prod build renders (D16): no entry, and so no way to reach the gallery.
        render(SettingsState.PREVIEW.copy(debugMenuEnabled = false))

        compose.onNodeWithTag("settings_debugMenuButton").assertDoesNotExist()
    }

    @Test
    fun `tapping the debug menu reports it as an event`() {
        render(SettingsState.PREVIEW.copy(debugMenuEnabled = true))

        compose.onNodeWithTag("settings_debugMenuButton").performClick()

        assertEquals(listOf(SettingsEvent.DebugMenuClicked), events)
    }
}
