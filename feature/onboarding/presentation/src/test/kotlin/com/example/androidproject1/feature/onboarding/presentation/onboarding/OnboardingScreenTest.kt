package com.example.androidproject1.feature.onboarding.presentation.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.androidproject1.core.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
class OnboardingScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<OnboardingEvent>()

    private fun render(state: OnboardingState) {
        compose.setContent {
            AppTheme {
                OnboardingScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders the pager and both actions`() {
        render(OnboardingState.PREVIEW)

        compose.onNodeWithTag("onboarding_pageList").assertIsDisplayed()
        compose.onNodeWithTag("onboarding_nextButton").assertIsDisplayed()
        compose.onNodeWithTag("onboarding_skipButton").assertIsDisplayed()
    }

    @Test
    fun `the pager reports the page it opened on`() {
        // The screen keeps the pager and the state in step in both directions; this is the
        // direction that would otherwise leave the ViewModel guessing.
        render(OnboardingState.PREVIEW)

        assertTrue(events.contains(OnboardingEvent.PageChanged(0)))
    }

    @Test
    fun `tapping next reports it as an event`() {
        render(OnboardingState.PREVIEW)
        events.clear()

        compose.onNodeWithTag("onboarding_nextButton").performClick()

        assertEquals(listOf(OnboardingEvent.NextClicked), events)
    }

    @Test
    fun `tapping skip reports it as an event`() {
        render(OnboardingState.PREVIEW)
        events.clear()

        compose.onNodeWithTag("onboarding_skipButton").performClick()

        assertEquals(listOf(OnboardingEvent.SkipClicked), events)
    }
}
