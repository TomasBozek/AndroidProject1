package com.example.androidproject1.feature.gallery.presentation.gallery

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.androidproject1.core.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * What is on screen, and what a tap does — the half `GalleryViewModelTest` cannot reach.
 *
 * The rows are asserted against a fixture of three rather than the real catalog: a `LazyColumn`
 * composes what fits, so counting the full list would be counting the window's height.
 *
 * **Everything is found by `testTag`, never by text.** A component's name in the gallery is copy
 * like any other.
 */
@RunWith(RobolectricTestRunner::class)
class GalleryScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<GalleryEvent>()

    private val threeRows = GalleryState(
        items = listOf(
            GalleryItem(id = "button", name = "Button", group = "Actions", summary = "A button"),
            GalleryItem(id = "tag", name = "Tag", group = "Actions", summary = "A tag"),
            GalleryItem(id = "text", name = "Text", group = "Typography", summary = "A text"),
        ),
    )

    private fun render(state: GalleryState) {
        compose.setContent {
            AppTheme {
                GalleryScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders the catalog it is given`() {
        render(GalleryState.PREVIEW)

        compose.onNodeWithTag("GalleryScreen").assertIsDisplayed()
        compose.onNodeWithTag("gallery_list").assertIsDisplayed()
        compose.onAllNodesWithTag("gallery_item")[0].assertIsDisplayed()
    }

    @Test
    fun `renders a row per component`() {
        render(threeRows)

        compose.onAllNodesWithTag("gallery_item").assertCountEquals(threeRows.items.size)
    }

    @Test
    fun `groups the rows the way the design system groups them`() {
        // The screen's own arithmetic: two groups out of three rows, each with a header above it.
        assertEquals(listOf("Actions", "Typography"), threeRows.groups.map { it.first })
        assertEquals(2, threeRows.groups.first().second.size)
    }

    @Test
    fun `an empty catalog still renders the screen`() {
        render(GalleryState(items = emptyList()))

        compose.onNodeWithTag("GalleryScreen").assertIsDisplayed()
        compose.onAllNodesWithTag("gallery_item").assertCountEquals(0)
    }

    @Test
    fun `tapping a component reports it as an event`() {
        render(threeRows)

        compose.onAllNodesWithTag("gallery_item")[0].performClick()

        assertEquals(listOf(GalleryEvent.ComponentClicked("button")), events)
    }
}
