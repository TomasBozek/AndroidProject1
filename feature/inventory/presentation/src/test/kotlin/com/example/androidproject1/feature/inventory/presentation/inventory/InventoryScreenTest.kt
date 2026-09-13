package com.example.androidproject1.feature.inventory.presentation.inventory

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.inventory.domain.ItemTag
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// A phone's height, so the sheet's last control is on screen to be tapped.
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
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

    private val selecting = InventoryState.PREVIEW.copy(
        selectedIds = setOf("item-drill"),
        pendingIds = setOf("item-kettle"),
    )

    @Test
    fun `renders a row per item, the filter button and the sort button`() {
        render(InventoryState.PREVIEW)

        compose.onNodeWithTag("inventory_list").assertIsDisplayed()
        compose.onAllNodesWithTag("inventory_item").assertCountEquals(InventoryState.PREVIEW.items.size)
        compose.onNodeWithTag("inventory_filterButton").assertIsDisplayed()
        compose.onNodeWithTag("inventory_sortButton").assertIsDisplayed()
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
    fun `tapping a row reports the item, and holding it starts selection`() {
        render(InventoryState.PREVIEW)

        compose.onAllNodesWithTag("inventory_item")[0].performClick()
        compose.onAllNodesWithTag("inventory_item")[1].performTouchInput { longClick() }

        assertEquals(
            listOf(
                InventoryEvent.ItemClicked(InventoryState.PREVIEW.items[0]),
                InventoryEvent.ItemLongPressed(InventoryState.PREVIEW.items[1]),
            ),
            events,
        )
    }

    @Test
    fun `the new-item button reports its event`() {
        render(InventoryState.PREVIEW)

        compose.onNodeWithTag("inventory_newButton").performClick()

        assertEquals(listOf(InventoryEvent.NewItemClicked), events)
    }

    @Test
    fun `the filter button opens the sheet, whose controls report their events`() {
        render(InventoryState.PREVIEW.copy(filterSheetOpen = true, filter = ItemFilter(tags = setOf(ItemTag.Lent))))

        // The sheet is a window of its own and scrolls on a short screen, so the fields are
        // asserted present rather than in view.
        compose.onNodeWithTag("inventory_filterSheet").assertIsDisplayed()
        compose.onNodeWithTag("inventory_categoryField").assertExists()
        compose.onNodeWithTag("inventory_tagsGroup").assertExists()
        compose.onNodeWithTag("inventory_maxPriceField").assertExists()
        compose.onNodeWithTag("inventory_allTagsCheckbox").performClick()
        compose.onNodeWithTag("inventory_clearButton").performClick()

        assertEquals(
            listOf(InventoryEvent.FilterAllTagsChanged(checked = false), InventoryEvent.FilterCleared),
            events,
        )
    }

    @Test
    fun `the filter badge shows what is in force, and the button reports its event`() {
        render(InventoryState.PREVIEW.copy(filter = ItemFilter(tags = setOf(ItemTag.Lent))))

        compose.onNodeWithTag("inventory_filterBadge").assertIsDisplayed()
        compose.onNodeWithTag("inventory_filterButton").performClick()

        assertEquals(listOf(InventoryEvent.FilterClicked), events)
    }

    @Test
    fun `the sort menu reports the chosen sort`() {
        render(InventoryState.PREVIEW)

        compose.onNodeWithTag("inventory_sortButton").performClick()
        compose.onAllNodesWithTag("inventory_sortItem")[1].performClick()

        assertEquals(listOf(InventoryEvent.SortSelected(ItemSort.Price)), events)
    }

    @Test
    fun `selection replaces the top bar with the toolbar, checks the rows and spins the pending one`() {
        render(selecting)

        compose.onNodeWithTag("inventory_selectionGroup").assertIsDisplayed()
        compose.onNodeWithTag("inventory_selectAllCheckbox").assertIsDisplayed()
        // A row is one merged node — its checkbox and spinner are only findable in the unmerged tree.
        compose.onAllNodesWithTag(
            "inventory_itemCheckbox",
            useUnmergedTree = true,
        ).assertCountEquals(selecting.items.size)
        compose.onAllNodesWithTag("inventory_itemProgress", useUnmergedTree = true).assertCountEquals(1)
        compose.onAllNodesWithTag("inventory_newButton").assertCountEquals(0)
    }

    @Test
    fun `the toolbar's actions report their events`() {
        render(selecting)

        compose.onNodeWithTag("inventory_selectAllCheckbox").performClick()
        compose.onNodeWithTag("inventory_favouriteButton").performClick()
        compose.onNodeWithTag("inventory_deleteButton").performClick()

        // One of three is selected, so the master is indeterminate and reports `false`; the view
        // model reads that as "select the rest" — see InventoryViewModelTest.
        assertEquals(
            listOf(
                InventoryEvent.SelectAllChanged(checked = false),
                InventoryEvent.FavouriteSelectedClicked,
                InventoryEvent.DeleteSelectedClicked,
            ),
            events,
        )
    }
}
