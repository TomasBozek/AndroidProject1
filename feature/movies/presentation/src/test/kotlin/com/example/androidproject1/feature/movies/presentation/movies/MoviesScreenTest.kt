package com.example.androidproject1.feature.movies.presentation.movies

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import com.example.androidproject1.core.ui.component.PULL_TO_REFRESH_REFRESHING
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.movies.domain.Movie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    fun `renders the list it is given`() {
        render(MoviesState.PREVIEW)

        compose.onNodeWithTag("MoviesScreen").assertIsDisplayed()
        compose.onNodeWithTag("movies_list").assertIsDisplayed()
        assertEquals(
            MoviesState.PREVIEW.movies.size,
            compose.onAllNodesWithTag("movies_item").fetchSemanticsNodes().size,
        )
    }

    @Test
    fun `a row reports which movie was tapped`() {
        render(MoviesState.PREVIEW)

        compose.onAllNodesWithTag("movies_item")[1].performClick()

        assertEquals(
            listOf(MoviesEvent.MovieClicked(MoviesState.PREVIEW.movies[1].id)),
            events.filterIsInstance<MoviesEvent.MovieClicked>(),
        )
    }

    @Test
    fun `reaching the end of a long list asks for more, once`() {
        val movies = List(LONG_LIST) { index ->
            Movie(id = index, title = "Movie $index", overview = "", posterUrl = null, releaseDate = null, rating = 7.0)
        }
        render(MoviesState.PREVIEW.copy(movies = movies, page = 1, totalPages = 3))

        compose.onNodeWithTag("movies_list").performScrollToIndex(LONG_LIST - 1)
        compose.waitForIdle()

        assertEquals(1, events.count { it == MoviesEvent.LoadMore })
    }

    @Test
    fun `a short list does not ask for more from the top`() {
        // Three rows fit on screen, so the last row is visible from the first frame — and that
        // is still the end of what there is, so the ask goes out. What must not happen is two.
        render(MoviesState.PREVIEW)
        compose.waitForIdle()

        assertTrue(events.count { it == MoviesEvent.LoadMore } <= 1)
    }

    @Test
    fun `a page on its way shows the footer spinner`() {
        render(MoviesState.PREVIEW.copy(loadingMore = true))

        compose.onNodeWithTag("movies_list").performScrollToIndex(MoviesState.PREVIEW.movies.size)
        compose.onNodeWithTag("movies_moreProgress").assertIsDisplayed()
    }

    @Test
    fun `a refresh in flight is on the box's state`() {
        render(MoviesState.PREVIEW.copy(refreshing = true))

        compose.onNode(hasStateDescription(PULL_TO_REFRESH_REFRESHING)).assertIsDisplayed()
    }

    @Test
    fun `an empty list shows the empty state, with the shell standing`() {
        render(MoviesState(loaded = true))

        compose.onNodeWithTag("movies_empty").assertIsDisplayed()
        compose.onNodeWithTag("movies_upButton").assertIsDisplayed()
        compose.onNode(hasTestTag("movies_list")).assertDoesNotExist()
    }

    @Test
    fun `a list not yet loaded shows neither rows nor the empty state`() {
        render(MoviesState())

        compose.onNode(hasTestTag("movies_empty")).assertDoesNotExist()
        assertEquals(0, compose.onAllNodesWithTag("movies_item").fetchSemanticsNodes().size)
    }

    @Test
    fun `up reports the event`() {
        render(MoviesState.PREVIEW)

        compose.onNodeWithTag("movies_upButton").performClick()

        assertEquals(listOf(MoviesEvent.NavigateUpClicked), events.filterIsInstance<MoviesEvent.NavigateUpClicked>())
    }

    private companion object {

        const val LONG_LIST = 40
    }
}
