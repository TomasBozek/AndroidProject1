package com.example.androidproject1.feature.settings.presentation.settings

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

/**
 * What is on screen, and what a tap does — the half `SettingsViewModelTest` cannot reach.
 *
 * Four buttons, four events, and the one thing a ViewModel test cannot see: that each button is
 * wired to its own event rather than to the one above it.
 *
 * **Everything is found by `testTag`, never by text.**
 */
private const val ROBOLECTRIC_SDK = 35

@RunWith(RobolectricTestRunner::class)
// Robolectric ships an SDK image per API level and has none for this project's targetSdk, so the
// level is pinned to the newest it does have. Raise it when Robolectric catches up; nothing in a
// screen test depends on the difference.
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
    fun `renders the account and every entry`() {
        render(SettingsState.PREVIEW)

        compose.onNodeWithTag("SettingsScreen").assertIsDisplayed()
        compose.onNodeWithTag("settings_emailValue").assertIsDisplayed()
        compose.onNodeWithTag("settings_profileButton").assertIsDisplayed()
        compose.onNodeWithTag("settings_componentsButton").assertIsDisplayed()
        compose.onNodeWithTag("settings_permissionsButton").assertIsDisplayed()
        compose.onNodeWithTag("settings_logoutButton").assertIsDisplayed()
    }

    @Test
    fun `a session with no email still renders the row`() {
        // The row is a placeholder rather than nothing: a missing email is a fact about the
        // session, and a row that disappears moves every button under it.
        render(SettingsState(email = null))

        compose.onNodeWithTag("settings_emailValue").assertIsDisplayed()
    }

    @Test
    fun `each entry reports its own event`() {
        render(SettingsState.PREVIEW)

        compose.onNodeWithTag("settings_profileButton").performClick()
        compose.onNodeWithTag("settings_componentsButton").performClick()
        compose.onNodeWithTag("settings_permissionsButton").performClick()
        compose.onNodeWithTag("settings_logoutButton").performClick()

        assertEquals(
            listOf(
                SettingsEvent.ProfileClicked,
                SettingsEvent.ComponentsClicked,
                SettingsEvent.PermissionsClicked,
                SettingsEvent.LogoutClicked,
            ),
            events,
        )
    }
}
