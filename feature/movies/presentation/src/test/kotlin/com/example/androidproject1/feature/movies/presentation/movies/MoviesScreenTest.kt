package com.example.androidproject1.feature.movies.presentation.movies

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

/**
 * What is on screen, and what a tap does.
 *
 * The other half of a screen's tests: `MoviesViewModelTest` says what the state becomes, this
 * says what the user sees and what their tap reports. It renders the stateless screen with a
 * fixed state and collects the events it emits, so nothing here needs a ViewModel, Koin or an
 * emulator — Robolectric makes it an ordinary unit test that `./gradlew test` picks up.
 *
 * **Everything is found by `testTag`, never by text.** Copy gets reworded and translated; a test
 * that finds a button by its label fails on a wording change that broke nothing.
 */
@RunWith(RobolectricTestRunner::class)
class MoviesScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<MoviesEvent>()

    private fun render(state: MoviesState) {
        compose.setContent {
            AppTheme {
                MoviesScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders the state it is given`() {
        render(MoviesState.PREVIEW)

        compose.onNodeWithTag("movies_titleValue").assertIsDisplayed()
        compose.onNodeWithTag("movies_counterValue").assertIsDisplayed()
        compose.onNodeWithTag("movies_incrementButton").assertIsDisplayed()
    }

    @Test
    fun `renders the empty state without collapsing`() {
        // The state a layout is most likely to break in, and one of the three in
        // MoviesStatePreviews. Replace it with this screen's real empty state; keep the test.
        render(MoviesState.PREVIEW.copy(title = "", counter = 0))

        compose.onNodeWithTag("movies_titleValue").assertIsDisplayed()
        compose.onNodeWithTag("movies_incrementButton").assertIsDisplayed()
    }

    @Test
    fun `tapping increment reports it as an event`() {
        render(MoviesState.PREVIEW)

        compose.onNodeWithTag("movies_incrementButton").performClick()

        assertEquals(listOf(MoviesEvent.IncrementClicked), events)
    }
}
