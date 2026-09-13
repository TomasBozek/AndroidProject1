package com.example.androidproject1.feature.inventory.presentation.inventory

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.androidproject1.core.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InventoryScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<InventoryEvent>()

    private fun render(state: InventoryState) {
        compose.setContent {
            AppTheme {
                InventoryScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders a row per item`() {
        render(InventoryState.PREVIEW)

        compose.onNodeWithTag("inventory_list").assertIsDisplayed()
        compose.onAllNodesWithTag("inventory_item").assertCountEquals(InventoryState.PREVIEW.items.size)
    }

    @Test
    fun `loading draws skeleton rows`() {
        render(InventoryState.PREVIEW.copy(loading = true))

        compose.onNodeWithTag("inventory_skeleton").assertIsDisplayed()
    }

    @Test
    fun `no match shows the empty state instead of the list`() {
        render(InventoryState.PREVIEW.copy(query = "piano", items = emptyList()))

        compose.onNodeWithTag("inventory_empty").assertIsDisplayed()
    }

    @Test
    fun `typing in the search field reports the query`() {
        render(InventoryState.PREVIEW)

        // The tag is on the field's frame; the input is the node inside it that takes text.
        compose.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("inventory_searchField")))
            .performTextInput("ke")

        assertEquals(listOf(InventoryEvent.QueryChanged("ke")), events)
    }

    @Test
    fun `tapping a row reports the item`() {
        render(InventoryState.PREVIEW)

        compose.onAllNodesWithTag("inventory_item")[0].performClick()

        assertEquals(listOf(InventoryEvent.ItemClicked(InventoryState.PREVIEW.items[0])), events)
    }

    @Test
    fun `the new-item button reports its event`() {
        render(InventoryState.PREVIEW)

        compose.onNodeWithTag("inventory_newButton").performClick()

        assertEquals(listOf(InventoryEvent.NewItemClicked), events)
    }
}
