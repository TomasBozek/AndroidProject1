package com.example.androidproject1.feature.template.presentation

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
 * What is on screen, and what a tap does.
 *
 * The other half of a screen's tests: `TemplateViewModelTest` says what the state becomes, this
 * says what the user sees and what their tap reports. It renders the stateless screen with a
 * fixed state and collects the events it emits, so nothing here needs a ViewModel, Koin or an
 * emulator — Robolectric makes it an ordinary unit test that `./gradlew test` picks up.
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
class TemplateScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<TemplateEvent>()

    private fun render(state: TemplateState) {
        compose.setContent {
            AppTheme {
                TemplateScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders the state it is given`() {
        render(TemplateState.PREVIEW)

        compose.onNodeWithTag("template_titleValue").assertIsDisplayed()
        compose.onNodeWithTag("template_counterValue").assertIsDisplayed()
        compose.onNodeWithTag("template_incrementButton").assertIsDisplayed()
    }

    @Test
    fun `renders the empty state without collapsing`() {
        // The state a layout is most likely to break in, and one of the three in
        // TemplateStatePreviews. Replace it with this screen's real empty state; keep the test.
        render(TemplateState.PREVIEW.copy(title = "", counter = 0))

        compose.onNodeWithTag("template_titleValue").assertIsDisplayed()
        compose.onNodeWithTag("template_incrementButton").assertIsDisplayed()
    }

    @Test
    fun `tapping increment reports it as an event`() {
        render(TemplateState.PREVIEW)

        compose.onNodeWithTag("template_incrementButton").performClick()

        assertEquals(listOf(TemplateEvent.IncrementClicked), events)
    }
}
