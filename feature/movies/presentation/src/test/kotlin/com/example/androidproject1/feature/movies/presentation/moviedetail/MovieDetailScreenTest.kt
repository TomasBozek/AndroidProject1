package com.example.androidproject1.feature.movies.presentation.moviedetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
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
 * What is on screen, and what a tap does. Everything is found by `testTag`; the genre names are
 * the one text on the right-hand side of an assertion, because the content *is* what is under
 * test there — that every genre becomes a tag.
 */
@RunWith(RobolectricTestRunner::class)
class MovieDetailScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<MovieDetailEvent>()

    private fun render(state: MovieDetailState) {
        compose.setContent {
            AppTheme {
                MovieDetailScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders the detail it is given`() {
        render(MovieDetailState.PREVIEW)

        compose.onNodeWithTag("MovieDetailScreen").assertIsDisplayed()
        compose.onNodeWithTag("movieDetail_posterTile").assertIsDisplayed()
        compose.onNodeWithTag("movieDetail_factsList").assertIsDisplayed()
        compose.onNodeWithTag("movieDetail_overviewValue").assertIsDisplayed()
    }

    @Test
    fun `every genre is a tag in the group`() {
        render(MovieDetailState.PREVIEW)

        compose.onNodeWithTag("movieDetail_genreGroup").assertIsDisplayed()
        MovieDetailState.PREVIEW.detail.genres.forEach { genre ->
            compose.onAllNodes(hasText(genre) and hasAnyAncestor(hasTestTag("movieDetail_genreGroup")))
                .fetchSemanticsNodes()
                .let { assertEquals("one tag for $genre", 1, it.size) }
        }
    }

    @Test
    fun `a movie with no genres and no overview keeps the shell standing`() {
        render(MovieDetailStatePreviews().values.elementAt(1))

        compose.onNodeWithTag("MovieDetailScreen").assertIsDisplayed()
        compose.onNodeWithTag("movieDetail_factsList").assertIsDisplayed()
        compose.onAllNodes(hasTestTag("movieDetail_overviewValue")).fetchSemanticsNodes().let {
            assertEquals(0, it.size)
        }
    }

    @Test
    fun `up reports the event`() {
        render(MovieDetailState.PREVIEW)

        compose.onNodeWithTag("movieDetail_upButton").performClick()

        assertEquals(listOf(MovieDetailEvent.NavigateUpClicked), events)
    }
}
