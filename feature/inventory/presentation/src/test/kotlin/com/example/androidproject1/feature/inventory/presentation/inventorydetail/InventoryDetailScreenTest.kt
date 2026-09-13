package com.example.androidproject1.feature.inventory.presentation.inventorydetail

import androidx.compose.ui.test.assertIsDisplayed
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
class InventoryDetailScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<InventoryDetailEvent>()

    private fun render(state: InventoryDetailState) {
        compose.setContent {
            AppTheme {
                InventoryDetailScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders the header, the sections and the menu`() {
        render(InventoryDetailState.PREVIEW)

        compose.onNodeWithTag("inventoryDetail_imageValue").assertIsDisplayed()
        compose.onNodeWithTag("inventoryDetail_ownerValue").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("inventoryDetail_sectionTab").assertIsDisplayed()
        compose.onNodeWithTag("inventoryDetail_menuButton").assertIsDisplayed()
    }

    @Test
    fun `loading draws skeleton rows`() {
        render(InventoryDetailState.PREVIEW.copy(item = null))

        compose.onNodeWithTag("inventoryDetail_skeleton").assertIsDisplayed()
    }

    @Test
    fun `the notes section shows the notes`() {
        render(InventoryDetailState.PREVIEW.copy(selectedSection = InventoryDetailState.SECTION_NOTES))

        compose.onNodeWithTag("inventoryDetail_notesValue").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `the history section lists what the item records`() {
        render(InventoryDetailState.PREVIEW.copy(selectedSection = InventoryDetailState.SECTION_HISTORY))

        compose.onNodeWithTag("inventoryDetail_historyList").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `the menu's edit and delete rows report their events`() {
        render(InventoryDetailState.PREVIEW)

        compose.onNodeWithTag("inventoryDetail_menuButton").performClick()
        compose.onNodeWithTag("inventoryDetail_editItem").performClick()
        compose.onNodeWithTag("inventoryDetail_menuButton").performClick()
        compose.onNodeWithTag("inventoryDetail_deleteItem").performClick()

        assertEquals(listOf(InventoryDetailEvent.EditClicked, InventoryDetailEvent.DeleteClicked), events)
    }

    @Test
    fun `the up arrow reports its event`() {
        render(InventoryDetailState.PREVIEW)

        compose.onNodeWithTag("inventoryDetail_upButton").performClick()

        assertEquals(listOf(InventoryDetailEvent.NavigateUpClicked), events)
    }
}
