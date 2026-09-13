package com.example.androidproject1.feature.inventory.presentation.inventory

import com.example.androidproject1.feature.inventory.domain.test.FakeInventoryRepository
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class InventoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeInventoryRepository()

    private fun viewModel() = InventoryViewModel(logger = FakeLogger(), inventoryRepository = repository)

    @Test
    fun `starts on skeleton rows and lands on the repository's items`() = runTest {
        val viewModel = viewModel()

        val state = viewModel.state.value.data!!
        assertFalse("the first emission has landed", state.loading)
        assertEquals(repository.current, state.items)
        // The shell is real, so the shared overlay never goes up.
        assertNull(viewModel.state.value.loading)
    }

    @Test
    fun `a query filters by name, ignoring case, and keeps the query`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(InventoryEvent.QueryChanged("KETTLE"))

        val state = viewModel.state.value.data!!
        assertEquals("KETTLE", state.query)
        assertEquals(listOf(FakeInventoryRepository.KETTLE), state.items)
    }

    @Test
    fun `a query nothing matches leaves the list empty rather than raising an error`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(InventoryEvent.QueryChanged("piano"))

        assertTrue(viewModel.state.value.data!!.items.isEmpty())
        assertNull(viewModel.state.value.content)
    }

    @Test
    fun `a write that arrives while a query is set is filtered too`() = runTest {
        val viewModel = viewModel()
        viewModel.onUiEvent(InventoryEvent.QueryChanged("drill"))

        repository.saveItem(FakeInventoryRepository.KETTLE.copy(id = "item-drill-2", name = "Hammer drill"))

        assertEquals(listOf("Cordless drill", "Hammer drill"), viewModel.state.value.data!!.items.map { it.name })
    }

    @Test
    fun `tapping an item opens it`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(InventoryEvent.ItemClicked(FakeInventoryRepository.DRILL))

        assertEquals(InventoryNavigation.OpenItem("item-drill"), viewModel.navigation.first())
    }

    @Test
    fun `the new-item button asks for the editor`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(InventoryEvent.NewItemClicked)

        assertEquals(InventoryNavigation.NewItem, viewModel.navigation.first())
    }
}
