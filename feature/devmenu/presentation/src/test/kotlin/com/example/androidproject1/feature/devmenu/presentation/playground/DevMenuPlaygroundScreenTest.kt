package com.example.androidproject1.feature.devmenu.presentation.playground

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.devmenu.presentation.KnobValue
import com.example.androidproject1.feature.devmenu.presentation.defaults
import com.example.androidproject1.feature.devmenu.presentation.playgroundEntry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * What is on screen, and what a tap does. The knob controls are reached by the ids built from
 * their keys — never by a label, which is data an entry may reword.
 */
@RunWith(RobolectricTestRunner::class)
class DevMenuPlaygroundScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<DevMenuPlaygroundEvent>()

    private fun render(state: DevMenuPlaygroundState) {
        compose.setContent {
            AppTheme {
                DevMenuPlaygroundScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders the picker, the stage and one control per knob`() {
        render(DevMenuPlaygroundState.PREVIEW)

        compose.onNodeWithTag("DevMenuPlaygroundScreen").assertIsDisplayed()
        compose.onNodeWithTag("devMenuPlayground_componentField").assertIsDisplayed()
        compose.onNodeWithTag("devMenuPlayground_stageCard").assertIsDisplayed()
        // The button entry's five knobs, each the control its shape says.
        compose.onNodeWithTag("devMenuPlayground_labelField").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("devMenuPlayground_kindTab").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("devMenuPlayground_sizeTab").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("devMenuPlayground_enabledSwitch").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("devMenuPlayground_loadingSwitch").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `another entry draws another set of controls`() {
        val stepper = playgroundEntry("stepper")!!
        render(DevMenuPlaygroundState(stepper.id, stepper.defaults()))

        compose.onNodeWithTag("devMenuPlayground_valueField").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("devMenuPlayground_minField").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("devMenuPlayground_maxField").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `turning a knob reports its key and the new value`() {
        render(DevMenuPlaygroundState.PREVIEW)

        compose.onNodeWithTag("devMenuPlayground_enabledSwitch").performScrollTo().performClick()

        assertEquals(listOf(DevMenuPlaygroundEvent.KnobChanged("enabled", KnobValue.Bool(false))), events)
    }

    @Test
    fun `up reports itself`() {
        render(DevMenuPlaygroundState.PREVIEW)

        compose.onNodeWithTag("devMenuPlayground_upButton").performClick()

        assertEquals(listOf(DevMenuPlaygroundEvent.NavigateUpClicked), events)
    }
}
