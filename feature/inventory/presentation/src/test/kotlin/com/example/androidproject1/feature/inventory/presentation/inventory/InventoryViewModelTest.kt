package com.example.androidproject1.feature.inventory.presentation.inventory

import androidx.lifecycle.SavedStateHandle
import com.example.androidproject1.core.ui.component.CheckState
import com.example.androidproject1.feature.inventory.domain.ItemCategory
import com.example.androidproject1.feature.inventory.domain.ItemTag
import com.example.androidproject1.feature.inventory.domain.test.FakeInventoryRepository
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.ui.event.SystemEvent
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

class InventoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeInventoryRepository()

    private fun viewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) = InventoryViewModel(
        logger = FakeLogger(),
        savedStateHandle = savedStateHandle,
        inventoryRepository = repository,
    )

    private val InventoryViewModel.data get() = state.value.data!!

    @Test
    fun `starts on skeleton rows and lands on the repository's items, sorted by name`() = runTest {
        val viewModel = viewModel()

        val state = viewModel.data
        assertFalse("the first emission has landed", state.loading)
        assertEquals(listOf("Cordless drill", "Electric kettle"), state.items.map { it.name })
        // The shell is real, so the shared overlay never goes up.
        assertNull(viewModel.state.value.loading)
    }

    @Test
    fun `a query filters by name, ignoring case, and keeps the query`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(InventoryEvent.QueryChanged("KETTLE"))

        val state = viewModel.data
        assertEquals("KETTLE", state.query)
        assertEquals(listOf(FakeInventoryRepository.KETTLE), state.items)
    }

    @Test
    fun `a query nothing matches leaves the list empty rather than raising an error`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(InventoryEvent.QueryChanged("piano"))

        assertTrue(viewModel.data.items.isEmpty())
        assertNull(viewModel.state.value.content)
    }

    @Test
    fun `a write that arrives while a query is set is filtered too`() = runTest {
        val viewModel = viewModel()
        viewModel.onUiEvent(InventoryEvent.QueryChanged("drill"))

        repository.saveItem(FakeInventoryRepository.KETTLE.copy(id = "item-drill-2", name = "Hammer drill"))

        assertEquals(listOf("Cordless drill", "Hammer drill"), viewModel.data.items.map { it.name })
    }

    @Test
    fun `tapping an item opens it, and the new-item button asks for the editor`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(InventoryEvent.ItemClicked(FakeInventoryRepository.DRILL))
        assertEquals(InventoryNavigation.OpenItem("item-drill"), viewModel.navigation.first())

        viewModel.onUiEvent(InventoryEvent.NewItemClicked)
        assertEquals(InventoryNavigation.NewItem, viewModel.navigation.first())
    }

    // --- filter and sort ---

    @Test
    fun `the filter narrows by category, tags and price, and the badge counts what is in force`() = runTest {
        val viewModel = viewModel()
        assertEquals(0, viewModel.data.filter.activeCount)

        viewModel.onUiEvent(InventoryEvent.FilterCategorySelected(ItemCategory.Electronics))
        assertEquals(listOf(FakeInventoryRepository.KETTLE), viewModel.data.items)
        assertEquals(1, viewModel.data.filter.activeCount)

        viewModel.onUiEvent(InventoryEvent.FilterTagChanged(ItemTag.Lent, checked = true))
        assertTrue("nothing is electronics and lent", viewModel.data.items.isEmpty())
        assertEquals(2, viewModel.data.filter.activeCount)

        viewModel.onUiEvent(InventoryEvent.FilterCleared)
        assertEquals(0, viewModel.data.filter.activeCount)
        assertEquals(2, viewModel.data.items.size)

        viewModel.onUiEvent(InventoryEvent.FilterMaxPriceChanged(0.05f))
        assertEquals(listOf(FakeInventoryRepository.KETTLE), viewModel.data.items)
    }

    @Test
    fun `the sheet's master checkbox is off, indeterminate and on`() = runTest {
        val viewModel = viewModel()
        assertEquals(CheckState.Off, viewModel.data.allFilterTagsState)

        viewModel.onUiEvent(InventoryEvent.FilterTagChanged(ItemTag.Fragile, checked = true))
        assertEquals(CheckState.Indeterminate, viewModel.data.allFilterTagsState)

        viewModel.onUiEvent(InventoryEvent.FilterAllTagsChanged(checked = true))
        assertEquals(CheckState.On, viewModel.data.allFilterTagsState)
    }

    @Test
    fun `sort by price puts the dearest first`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(InventoryEvent.SortSelected(ItemSort.Price))

        assertEquals(listOf("Cordless drill", "Electric kettle"), viewModel.data.items.map { it.name })
        viewModel.onUiEvent(InventoryEvent.SortSelected(ItemSort.Name))
        assertEquals(ItemSort.Name, viewModel.data.sort)
    }

    @Test
    fun `the filter and the sort survive process death`() = runTest {
        val handle = SavedStateHandle()
        val first = viewModel(handle)
        first.onUiEvent(InventoryEvent.FilterCategorySelected(ItemCategory.Tools))
        first.onUiEvent(InventoryEvent.FilterTagChanged(ItemTag.Lent, checked = true))
        first.onUiEvent(InventoryEvent.FilterMaxPriceChanged(0.5f))
        first.onUiEvent(InventoryEvent.SortSelected(ItemSort.Acquired))

        // A new ViewModel over the same handle is what the process coming back looks like.
        val second = viewModel(handle)

        assertEquals(first.data.filter, second.data.filter)
        assertEquals(ItemSort.Acquired, second.data.sort)
        assertEquals(listOf(FakeInventoryRepository.DRILL), second.data.items)
    }

    // --- selection ---

    @Test
    fun `a long press starts selection, a tap then toggles, and clearing ends it`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(InventoryEvent.ItemLongPressed(FakeInventoryRepository.DRILL))
        assertTrue(viewModel.data.selecting)
        assertEquals(CheckState.Indeterminate, viewModel.data.selectAllState)

        // From "some", the master's own `false` means "the rest too".
        viewModel.onUiEvent(InventoryEvent.SelectAllChanged(checked = false))
        assertEquals(CheckState.On, viewModel.data.selectAllState)

        viewModel.onUiEvent(InventoryEvent.SelectAllChanged(checked = false))
        assertFalse(viewModel.data.selecting)
    }

    @Test
    fun `delete asks first with the ids, and a confirmed answer deletes them and settles the rows`() = runTest {
        val viewModel = viewModel()
        viewModel.onUiEvent(InventoryEvent.ItemLongPressed(FakeInventoryRepository.DRILL))

        viewModel.onUiEvent(InventoryEvent.DeleteSelectedClicked)
        val alert = viewModel.state.value.alert
        assertNotNull(alert)
        assertEquals(InventoryViewModel.ALERT_ID_DELETE, alert!!.id)
        assertNotNull("the alert carries what it is deleting", alert.payload)

        viewModel.onSystemEvent(SystemEvent.AlertResult.Confirmed(alert.id, alert.payload))

        assertEquals(setOf("item-drill"), repository.deletedIds)
        assertEquals(listOf(FakeInventoryRepository.KETTLE), viewModel.data.items)
        assertFalse("nothing left selected", viewModel.data.selecting)
        assertTrue("nothing left pending", viewModel.data.pendingIds.isEmpty())
    }

    @Test
    fun `favourite marks every selected row, and clears the mark once all of them carry it`() = runTest {
        val viewModel = viewModel()
        viewModel.onUiEvent(InventoryEvent.ItemLongPressed(FakeInventoryRepository.DRILL))
        viewModel.onUiEvent(InventoryEvent.ItemChecked(FakeInventoryRepository.KETTLE, checked = true))

        viewModel.onUiEvent(InventoryEvent.FavouriteSelectedClicked)
        assertTrue(repository.current.all { ItemTag.Favourite in it.tags })

        viewModel.onUiEvent(InventoryEvent.FavouriteSelectedClicked)
        assertTrue(repository.current.none { ItemTag.Favourite in it.tags })
        assertTrue(viewModel.data.pendingIds.isEmpty())
    }
}
