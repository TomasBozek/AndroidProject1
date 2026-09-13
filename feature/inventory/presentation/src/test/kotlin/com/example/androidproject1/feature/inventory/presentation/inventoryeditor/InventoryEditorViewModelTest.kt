package com.example.androidproject1.feature.inventory.presentation.inventoryeditor

import com.example.androidproject1.core.ui.component.CheckState
import com.example.androidproject1.feature.inventory.domain.ItemCategory
import com.example.androidproject1.feature.inventory.domain.ItemTag
import com.example.androidproject1.feature.inventory.domain.test.FakeInventoryRepository
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.ui.event.SystemEvent
import com.example.androidproject1.service.core.ui.form.ALERT_ID_DISCARD
import com.example.androidproject1.service.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class InventoryEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeInventoryRepository()

    private fun viewModel(itemId: String = "item-new") = InventoryEditorViewModel(
        logger = FakeLogger(),
        args = InventoryEditorDestination(itemId = itemId),
        inventoryRepository = repository,
    )

    private val InventoryEditorViewModel.data get() = state.value.data!!

    @Test
    fun `an id that names an item loads it into the form`() = runTest {
        val viewModel = viewModel(itemId = "item-drill")

        val state = viewModel.data
        assertTrue(state.editing)
        assertFalse(state.loading)
        assertEquals("Cordless drill", state.name.value)
        assertEquals(ItemCategory.Tools, state.category)
        assertEquals(setOf(ItemTag.Lent), state.tags)
        assertEquals(FakeInventoryRepository.DRILL.priceMinor, state.priceMinor)
    }

    @Test
    fun `an id that names nothing starts blank, and is not an error`() = runTest {
        val viewModel = viewModel(itemId = "item-new")

        val state = viewModel.data
        assertFalse(state.editing)
        assertFalse(state.loading)
        assertEquals("", state.name.value)
        assertNull(viewModel.state.value.alert)
    }

    @Test
    fun `basics cannot continue without a name, and next touches the field so the error shows`() = runTest {
        val viewModel = viewModel()

        assertFalse(viewModel.data.canContinue)
        viewModel.onUiEvent(InventoryEditorEvent.NextClicked)
        assertEquals(InventoryEditorState.STEP_BASICS, viewModel.data.step)
        assertNotNull("the required error is visible now", viewModel.data.name.error)

        viewModel.onUiEvent(InventoryEditorEvent.NameChanged("Lamp"))
        assertTrue(viewModel.data.canContinue)
        viewModel.onUiEvent(InventoryEditorEvent.NextClicked)
        assertEquals(InventoryEditorState.STEP_QUANTITY, viewModel.data.step)
    }

    @Test
    fun `quantity cannot continue at zero`() = runTest {
        val viewModel = viewModel(itemId = "item-drill")
        viewModel.onUiEvent(InventoryEditorEvent.NextClicked)
        assertEquals(InventoryEditorState.STEP_QUANTITY, viewModel.data.step)

        viewModel.onUiEvent(InventoryEditorEvent.QuantityChanged(0))
        assertFalse(viewModel.data.canContinue)

        viewModel.onUiEvent(InventoryEditorEvent.QuantityChanged(2))
        assertTrue(viewModel.data.canContinue)
    }

    @Test
    fun `the master checkbox is off, indeterminate and on as tags are ticked`() = runTest {
        val viewModel = viewModel()
        assertEquals(CheckState.Off, viewModel.data.allTagsState)

        viewModel.onUiEvent(InventoryEditorEvent.TagChanged(ItemTag.Fragile, checked = true))
        assertEquals(CheckState.Indeterminate, viewModel.data.allTagsState)

        viewModel.onUiEvent(InventoryEditorEvent.AllTagsChanged(checked = true))
        assertEquals(CheckState.On, viewModel.data.allTagsState)
        assertEquals(ItemTag.entries.toSet(), viewModel.data.tags)

        viewModel.onUiEvent(InventoryEditorEvent.AllTagsChanged(checked = false))
        assertEquals(CheckState.Off, viewModel.data.allTagsState)
    }

    @Test
    fun `saving from the review writes the item and pops`() = runTest {
        val viewModel = viewModel(itemId = "item-drill")
        viewModel.onUiEvent(InventoryEditorEvent.NameChanged("Hammer drill"))
        viewModel.onUiEvent(InventoryEditorEvent.InsuredChanged(true))
        repeat(3) { viewModel.onUiEvent(InventoryEditorEvent.NextClicked) }
        assertEquals(InventoryEditorState.STEP_REVIEW, viewModel.data.step)

        viewModel.onUiEvent(InventoryEditorEvent.NextClicked)

        val saved = repository.saved!!
        assertEquals("item-drill", saved.id)
        assertEquals("Hammer drill", saved.name)
        assertTrue(saved.insured)
        assertEquals(setOf(ItemTag.Lent), saved.tags)
        assertEquals(InventoryEditorNavigation.Saved, viewModel.navigation.first())
    }

    @Test
    fun `up from a later step goes back a step, and from the first leaves`() = runTest {
        val viewModel = viewModel(itemId = "item-drill")
        viewModel.onUiEvent(InventoryEditorEvent.NextClicked)

        viewModel.onUiEvent(InventoryEditorEvent.NavigateUpClicked)
        assertEquals(InventoryEditorState.STEP_BASICS, viewModel.data.step)

        viewModel.onUiEvent(InventoryEditorEvent.NavigateUpClicked)
        assertEquals(InventoryEditorNavigation.NavigateUp, viewModel.navigation.first())
    }

    @Test
    fun `a dirty form asks before discarding, and confirming leaves`() = runTest {
        val viewModel = viewModel()
        viewModel.onUiEvent(InventoryEditorEvent.NameChanged("Lamp"))

        viewModel.onUiEvent(InventoryEditorEvent.BackRequested)
        val alert = viewModel.state.value.alert
        assertNotNull(alert)
        assertEquals(ALERT_ID_DISCARD, alert!!.id)

        viewModel.onSystemEvent(SystemEvent.AlertResult.Confirmed(alert.id, alert.payload))
        assertNull(viewModel.state.value.alert)
        assertEquals(InventoryEditorNavigation.NavigateUp, viewModel.navigation.first())
    }
}
