package com.example.androidproject1.feature.inventory.presentation.inventory

import com.example.androidproject1.feature.inventory.domain.InventoryRepository
import com.example.androidproject1.feature.inventory.domain.Item
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.service.core.ui.viewmodel.ErrorDisplay

class InventoryViewModel(
    logger: Logger,
    private val inventoryRepository: InventoryRepository,
) : BaseViewModel<InventoryState, InventoryEvent, InventoryNavigation>(
    // The shell draws immediately; the rows are skeletons until the first emission lands.
    initialState = InventoryState(loading = true),
    logger = logger.withTag("InventoryViewModel"),
) {

    /** Every item, unfiltered: what the query is applied to on each keystroke and each emission. */
    private var allItems: List<Item> = emptyList()

    init {
        observeItems()
    }

    override fun onUiEvent(event: InventoryEvent) {
        when (event) {
            is InventoryEvent.QueryChanged -> updateData {
                copy(query = event.query, items = allItems.matching(event.query))
            }
            is InventoryEvent.ItemClicked -> navigate(InventoryNavigation.OpenItem(event.item.id))
            InventoryEvent.NewItemClicked -> navigate(InventoryNavigation.NewItem)
        }
    }

    private fun observeItems() = observe(
        flow = { inventoryRepository.observeItems() },
        errorDisplay = ErrorDisplay.Inline,
        onData = { items ->
            allItems = items
            updateData { copy(items = items.matching(query), loading = false) }
        },
    )

    // The filter lives here rather than in the screen, so a test can say what a query shows without
    // rendering anything, and the screen never has to know what "matches" means.
    private fun List<Item>.matching(query: String): List<Item> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return this
        return filter { it.name.contains(trimmed, ignoreCase = true) }
    }
}
