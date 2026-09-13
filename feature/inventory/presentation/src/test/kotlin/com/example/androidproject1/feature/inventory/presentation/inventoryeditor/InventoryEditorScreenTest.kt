package com.example.androidproject1.feature.inventory.presentation.inventoryeditor

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
class InventoryEditorScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<InventoryEditorEvent>()

    private fun render(state: InventoryEditorState) {
        compose.setContent {
            AppTheme {
                InventoryEditorScreen(state = state, onEvent = events::add)
            }
        }
    }

    /** The step's fields scroll; the button beneath them does not, so it is asserted as it is. */
    private fun assertOnScreen(vararg tags: String) {
        tags.forEach { compose.onNodeWithTag(it).performScrollTo().assertIsDisplayed() }
        compose.onNodeWithTag("inventoryEditor_nextButton").assertIsDisplayed()
    }

    @Test
    fun `the basics step carries its four fields, the progress and the up arrow`() {
        render(InventoryEditorState.PREVIEW)

        assertOnScreen(
            "inventoryEditor_stepProgress",
            "inventoryEditor_nameField",
            "inventoryEditor_categoryField",
            "inventoryEditor_conditionField",
            "inventoryEditor_acquiredField",
        )
        compose.onNodeWithTag("inventoryEditor_upButton").assertIsDisplayed()
    }

    @Test
    fun `the quantity step carries the stepper, the slider and the switch`() {
        render(InventoryEditorState.PREVIEW.copy(step = InventoryEditorState.STEP_QUANTITY))

        assertOnScreen("inventoryEditor_quantityField", "inventoryEditor_priceField", "inventoryEditor_insuredSwitch")
    }

    @Test
    fun `the tags step carries the group, its master, the owner, the picture and the notes`() {
        render(InventoryEditorState.PREVIEW.copy(step = InventoryEditorState.STEP_TAGS))

        assertOnScreen(
            "inventoryEditor_tagsGroup",
            "inventoryEditor_allTagsCheckbox",
            "inventoryEditor_ownerField",
            "inventoryEditor_imageField",
            "inventoryEditor_notesField",
        )
    }

    @Test
    fun `the review step carries the tile`() {
        render(InventoryEditorState.PREVIEW.copy(step = InventoryEditorState.STEP_REVIEW))

        assertOnScreen("inventoryEditor_reviewTile")
    }

    @Test
    fun `next reports its event`() {
        render(InventoryEditorState.PREVIEW)

        compose.onNodeWithTag("inventoryEditor_nextButton").performClick()

        assertEquals(listOf(InventoryEditorEvent.NextClicked), events)
    }

    @Test
    fun `the master checkbox reports the all-tags event`() {
        // No tag ticked, so the master is Off and a tap asks for all of them.
        render(InventoryEditorState.PREVIEW.copy(step = InventoryEditorState.STEP_TAGS, tags = emptySet()))

        compose.onNodeWithTag("inventoryEditor_allTagsCheckbox").performScrollTo().performClick()

        assertEquals(listOf(InventoryEditorEvent.AllTagsChanged(checked = true)), events)
    }

    @Test
    fun `the up arrow reports its event`() {
        render(InventoryEditorState.PREVIEW)

        compose.onNodeWithTag("inventoryEditor_upButton").performClick()

        assertEquals(listOf(InventoryEditorEvent.NavigateUpClicked), events)
    }
}
