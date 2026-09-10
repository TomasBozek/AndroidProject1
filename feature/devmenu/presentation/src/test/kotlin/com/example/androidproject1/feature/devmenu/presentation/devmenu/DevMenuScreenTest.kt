package com.example.androidproject1.feature.devmenu.presentation.devmenu

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.androidproject1.core.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DevMenuScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<DevMenuEvent>()

    private fun render(state: DevMenuState) {
        compose.setContent {
            AppTheme {
                DevMenuScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders the build, the session and the switches`() {
        render(DevMenuState.PREVIEW)

        // The menu is longer than the window, so the rows below the fold are scrolled to
        // rather than asserted where they happen to land.
        compose.onNodeWithTag("devMenu_buildList").assertIsDisplayed()
        compose.onNodeWithTag("devMenu_sessionValue").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("devMenu_offlineSwitch").performScrollTo().assertIsOff()
        compose.onNodeWithTag("devMenu_componentsButton").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("devMenu_notificationButton").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("devMenu_crashButton").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `a build with no fixture engine has no offline switch`() {
        render(DevMenuState.PREVIEW.copy(offlineSupported = false))

        compose.onNodeWithTag("devMenu_offlineSwitch").assertDoesNotExist()
    }

    @Test
    fun `toggling offline reports it as an event`() {
        render(DevMenuState.PREVIEW)

        compose.onNodeWithTag("devMenu_offlineSwitch").performScrollTo().performClick()

        assertEquals(listOf(DevMenuEvent.OfflineToggled(true)), events)
    }

    @Test
    fun `tapping the notification button reports it as an event`() {
        render(DevMenuState.PREVIEW)

        compose.onNodeWithTag("devMenu_notificationButton").performScrollTo().performClick()

        assertEquals(listOf(DevMenuEvent.NotificationClicked), events)
    }

    @Test
    fun `tapping components reports it as an event`() {
        render(DevMenuState.PREVIEW)

        compose.onNodeWithTag("devMenu_componentsButton").performScrollTo().performClick()

        assertEquals(listOf(DevMenuEvent.ComponentsClicked), events)
    }
}
