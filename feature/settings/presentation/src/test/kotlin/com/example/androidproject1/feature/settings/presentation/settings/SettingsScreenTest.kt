package com.example.androidproject1.feature.settings.presentation.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.settings.domain.ThemePreference
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * What is on screen, and what a tap does — the half `SettingsViewModelTest` cannot reach.
 *
 * The one thing a ViewModel test cannot see is that each button is wired to its own event rather
 * than to the one above it, which is what the entry test asserts.
 *
 * **Everything is found by `testTag`, never by text.**
 */

@RunWith(RobolectricTestRunner::class)
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

        // The list is longer than the window, so the entries below the fold are scrolled to
        // rather than asserted where they happen to land.
        compose.onNodeWithTag("SettingsScreen").assertIsDisplayed()
        compose.onNodeWithTag("settings_emailValue").assertIsDisplayed()
        compose.onNodeWithTag("settings_profileButton").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("settings_permissionsButton").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("settings_logoutButton").performScrollTo().assertIsDisplayed()
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

        compose.onNodeWithTag("settings_languageButton").performScrollTo().performClick()
        compose.onNodeWithTag("settings_profileButton").performScrollTo().performClick()
        compose.onNodeWithTag("settings_permissionsButton").performScrollTo().performClick()
        compose.onNodeWithTag("settings_logoutButton").performScrollTo().performClick()

        assertEquals(
            listOf(
                SettingsEvent.LanguageClicked,
                SettingsEvent.ProfileClicked,
                SettingsEvent.PermissionsClicked,
                SettingsEvent.LogoutClicked,
            ),
            events,
        )
    }

    /**
     * The options of the segmented control, in the order it draws them — which is
     * `ThemePreference.entries`. Reached through the group's tag rather than by label, because
     * the labels are copy and get translated.
     */
    private fun themeOptions() =
        compose.onAllNodes(isSelectable() and hasAnyAncestor(hasTestTag("settings_themeTab")))

    @Test
    fun `the stored theme is the selected option`() {
        render(SettingsState.PREVIEW.copy(theme = ThemePreference.Dark))

        themeOptions()[ThemePreference.Dark.ordinal].assertIsSelected()
    }

    @Test
    fun `picking a theme reports it as an event`() {
        render(SettingsState.PREVIEW.copy(theme = ThemePreference.System))

        themeOptions()[ThemePreference.Light.ordinal].performClick()

        assertEquals(listOf(SettingsEvent.ThemeSelected(ThemePreference.Light)), events)
    }

    @Test
    fun `a build with a debug menu offers the entry`() {
        render(SettingsState.PREVIEW.copy(debugMenuEnabled = true))

        compose.onNodeWithTag("settings_debugMenuButton").performScrollTo().assertIsDisplayed()
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

        compose.onNodeWithTag("settings_debugMenuButton").performScrollTo().performClick()

        assertEquals(listOf(SettingsEvent.DebugMenuClicked), events)
    }
}
