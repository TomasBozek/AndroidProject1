package com.example.androidproject1.feature.inventory.presentation.inventorydetail

import com.example.androidproject1.feature.inventory.domain.test.FakeInventoryRepository
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.ui.event.SystemEvent
import com.example.androidproject1.service.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class InventoryDetailViewModelTest {

    private companion object {

        const val TIMEOUT_MILLIS = 100L
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeInventoryRepository()

    private fun viewModel(itemId: String = "item-drill") = InventoryDetailViewModel(
        logger = FakeLogger(),
        args = InventoryDetailDestination(itemId = itemId),
        inventoryRepository = repository,
    )

    @Test
    fun `shows the item the route names`() = runTest {
        val state = viewModel().state.value

        assertEquals(FakeInventoryRepository.DRILL, state.data?.item)
        assertNull(state.loading)
    }

    @Test
    fun `an edit saved elsewhere shows up without a reload`() = runTest {
        val viewModel = viewModel()

        repository.saveItem(FakeInventoryRepository.DRILL.copy(name = "Hammer drill"))

        assertEquals("Hammer drill", viewModel.state.value.data?.item?.name)
    }

    @Test
    fun `an id that names nothing leaves`() = runTest {
        val viewModel = viewModel(itemId = "item-gone")

        assertEquals(InventoryDetailNavigation.NavigateUp, viewModel.navigation.first())
    }

    @Test
    fun `selecting a section keeps it`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(InventoryDetailEvent.SectionSelected(InventoryDetailState.SECTION_HISTORY))

        assertEquals(InventoryDetailState.SECTION_HISTORY, viewModel.state.value.data?.selectedSection)
    }

    @Test
    fun `edit opens the editor on this id`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(InventoryDetailEvent.EditClicked)

        assertEquals(InventoryDetailNavigation.Edit("item-drill"), viewModel.navigation.first())
    }

    @Test
    fun `delete asks first, carrying the id, and a confirmed answer deletes and leaves`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(InventoryDetailEvent.DeleteClicked)
        val alert = viewModel.state.value.alert
        assertNotNull(alert)
        assertNotNull("the alert carries what it is deleting", alert!!.payload)
        assertEquals(emptySet<String>(), repository.deletedIds)

        // What `Screen()` does when the confirm button is pressed: the payload comes back with it.
        viewModel.onSystemEvent(SystemEvent.AlertResult.Confirmed(alert.id, alert.payload))

        assertEquals(setOf("item-drill"), repository.deletedIds)
        assertEquals(InventoryDetailNavigation.NavigateUp, viewModel.navigation.first())
        // The delete and the observed `null` are one departure, not two: a second pop would take
        // the list below with it. The flow is a channel, so a second intent would be waiting here.
        assertNull(withTimeoutOrNull(TIMEOUT_MILLIS) { viewModel.navigation.first() })
    }

    @Test
    fun `a declined delete changes nothing`() = runTest {
        val viewModel = viewModel()
        viewModel.onUiEvent(InventoryDetailEvent.DeleteClicked)
        val alert = viewModel.state.value.alert!!

        viewModel.onSystemEvent(SystemEvent.AlertResult.Declined(alert.id, alert.payload))

        assertEquals(emptySet<String>(), repository.deletedIds)
        assertNull(viewModel.state.value.alert)
    }
}
