package com.example.androidproject1.feature.inventory.presentation.inventory

import androidx.lifecycle.SavedStateHandle
import com.example.androidproject1.core.ui.component.CheckState
import com.example.androidproject1.feature.inventory.domain.InventoryRepository
import com.example.androidproject1.feature.inventory.domain.Item
import com.example.androidproject1.feature.inventory.domain.ItemCategory
import com.example.androidproject1.feature.inventory.domain.ItemTag
import com.example.androidproject1.feature.inventory.presentation.R
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.domain.result.Outcome
import com.example.androidproject1.service.core.ui.event.SystemEvent
import com.example.androidproject1.service.core.ui.state.AlertPayload
import com.example.androidproject1.service.core.ui.state.clearAlert
import com.example.androidproject1.service.core.ui.state.setAlert
import com.example.androidproject1.service.core.ui.text.toPluralUiText
import com.example.androidproject1.service.core.ui.text.toUiText
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.service.core.ui.viewmodel.ErrorDisplay
import java.time.LocalDate
import kotlin.math.roundToLong

class InventoryViewModel(
    logger: Logger,
    savedStateHandle: SavedStateHandle,
    private val inventoryRepository: InventoryRepository,
) : BaseViewModel<InventoryState, InventoryEvent, InventoryNavigation>(
    // The shell draws immediately; the rows are skeletons until the first emission lands.
    initialState = InventoryState(loading = true),
    logger = logger.withTag("InventoryViewModel"),
    savedStateHandle = savedStateHandle,
) {

    /** What the delete alert carries back: the rows it was asked about, not whatever is selected now. */
    private data class DeleteItems(val itemIds: Set<String>) : AlertPayload

    /** Every item, unfiltered: what the query, the filter and the sort are applied to. */
    private var allItems: List<Item> = emptyList()

    // The filter and the sort survive process death: a filter someone set is exactly the state
    // they notice losing. Saved as plain strings, which is what a SavedStateHandle carries, and
    // decoded once here — the state holds the typed value.
    private var savedCategory: String? by saved(KEY_CATEGORY, null)
    private var savedTags: String by saved(KEY_TAGS, "")
    private var savedMaxPrice: Long by saved(KEY_MAX_PRICE, NO_CAP)
    private var savedSort: String by saved(KEY_SORT, ItemSort.Name.name)

    init {
        updateData {
            copy(
                filter = ItemFilter(
                    category = savedCategory?.let { name -> ItemCategory.entries.firstOrNull { it.name == name } },
                    tags = savedTags.split(SEPARATOR).mapNotNull { name ->
                        ItemTag.entries.firstOrNull { it.name == name }
                    }.toSet(),
                    maxPriceMinor = savedMaxPrice.takeIf { it != NO_CAP },
                ),
                sort = ItemSort.entries.firstOrNull { it.name == savedSort } ?: ItemSort.Name,
            )
        }
        observeItems()
    }

    override fun onSystemEvent(event: SystemEvent) {
        if (event is SystemEvent.AlertResult.Confirmed && event.id == ALERT_ID_DELETE) {
            uiState.clearAlert()
            (event.payload as? DeleteItems)?.let { deleteItems(it.itemIds) }
            return
        }
        super.onSystemEvent(event)
    }

    override fun onUiEvent(event: InventoryEvent) {
        when (event) {
            is InventoryEvent.QueryChanged -> updateData { copy(query = event.query).refreshed() }
            is InventoryEvent.ItemClicked -> onItemClicked(event.item)
            InventoryEvent.NewItemClicked -> navigate(InventoryNavigation.NewItem)

            InventoryEvent.FilterClicked -> updateData { copy(filterSheetOpen = true) }
            InventoryEvent.FilterDismissed -> updateData { copy(filterSheetOpen = false) }
            is InventoryEvent.FilterCategorySelected -> updateFilter { copy(category = event.category) }
            is InventoryEvent.FilterTagChanged -> updateFilter {
                copy(tags = if (event.checked) tags + event.tag else tags - event.tag)
            }
            // A tri-state master that is neither on nor off goes to on when tapped — some selected
            // means "select the rest", never "clear what I chose" — so the checkbox's own `false`
            // from that state is read as "all".
            is InventoryEvent.FilterAllTagsChanged -> updateFilter {
                val all = event.checked || tags.isNotEmpty() && tags.size < ItemTag.entries.size
                copy(tags = if (all) ItemTag.entries.toSet() else emptySet())
            }
            is InventoryEvent.FilterMaxPriceChanged -> updateFilter {
                copy(maxPriceMinor = maxPriceFor(event.fraction))
            }
            InventoryEvent.FilterCleared -> updateFilter { ItemFilter() }
            is InventoryEvent.SortSelected -> {
                savedSort = event.sort.name
                updateData { copy(sort = event.sort).refreshed() }
            }

            is InventoryEvent.ItemLongPressed -> updateData { copy(selectedIds = selectedIds + event.item.id) }
            is InventoryEvent.ItemChecked -> updateData {
                copy(selectedIds = if (event.checked) selectedIds + event.item.id else selectedIds - event.item.id)
            }
            is InventoryEvent.SelectAllChanged -> updateData {
                // The same rule as the filter's master: from "some", a tap selects the rest.
                val all = event.checked || selectAllState == CheckState.Indeterminate
                copy(selectedIds = if (all) items.map { it.id }.toSet() else emptySet())
            }
            InventoryEvent.SelectionCleared -> updateData { copy(selectedIds = emptySet()) }
            InventoryEvent.DeleteSelectedClicked -> askToDelete()
            InventoryEvent.FavouriteSelectedClicked -> toggleFavourite()
        }
    }

    // In selection mode a tap toggles the row, the way a long press started it; otherwise it opens.
    private fun onItemClicked(item: Item) {
        val state = uiState.value.data ?: return
        if (state.selecting) {
            onUiEvent(InventoryEvent.ItemChecked(item, checked = item.id !in state.selectedIds))
        } else {
            navigate(InventoryNavigation.OpenItem(item.id))
        }
    }

    private fun updateFilter(change: ItemFilter.() -> ItemFilter) {
        updateData {
            val filter = filter.change()
            savedCategory = filter.category?.name
            savedTags = filter.tags.joinToString(SEPARATOR) { it.name }
            savedMaxPrice = filter.maxPriceMinor ?: NO_CAP
            copy(filter = filter).refreshed()
        }
    }

    private fun askToDelete() {
        val ids = uiState.value.data?.selectedIds.orEmpty()
        if (ids.isEmpty()) return
        uiState.setAlert(
            id = ALERT_ID_DELETE,
            title = R.plurals.inventory_delete_title.toPluralUiText(ids.size, ids.size),
            message = R.string.inventory_delete_message.toUiText(),
            confirmLabel = R.string.inventory_delete_confirm.toUiText(),
            declineLabel = R.string.inventory_delete_cancel.toUiText(),
            payload = DeleteItems(ids),
        )
    }

    private fun deleteItems(ids: Set<String>) {
        updateData { copy(pendingIds = pendingIds + ids) }
        execute(
            action = { inventoryRepository.deleteItems(ids) },
            onError = {
                settle(ids)
                false
            },
            onData = {
                updateData { copy(selectedIds = selectedIds - ids) }
                settle(ids)
            },
        )
    }

    // Every selected row becomes a favourite, unless all of them already are — then none is.
    private fun toggleFavourite() {
        val state = uiState.value.data ?: return
        val selected = allItems.filter { it.id in state.selectedIds }
        if (selected.isEmpty()) return
        val favourite = !selected.all { ItemTag.Favourite in it.tags }
        val ids = selected.map { it.id }.toSet()
        updateData { copy(pendingIds = pendingIds + ids) }
        execute(
            action = {
                selected.forEach { item ->
                    val tags = if (favourite) item.tags + ItemTag.Favourite else item.tags - ItemTag.Favourite
                    val outcome = inventoryRepository.saveItem(item.copy(tags = tags))
                    if (outcome is Outcome.Failure) return@execute outcome
                }
                Outcome.Success(Unit)
            },
            onError = {
                settle(ids)
                false
            },
            onData = { settle(ids) },
        )
    }

    private fun settle(ids: Set<String>) = updateData { copy(pendingIds = pendingIds - ids) }

    private fun observeItems() = observe(
        flow = { inventoryRepository.observeItems() },
        errorDisplay = ErrorDisplay.Inline,
        onData = { items ->
            allItems = items
            updateData {
                // A row that is gone cannot stay selected or pending.
                val present = items.map { it.id }.toSet()
                copy(
                    loading = false,
                    selectedIds = selectedIds intersect present,
                    pendingIds =
                    pendingIds intersect present,
                )
                    .refreshed()
            }
        },
    )

    // The filter, the query and the sort live here rather than in the screen, so a test can say
    // what a query shows without rendering anything, and the screen never has to know what
    // "matches" means.
    private fun InventoryState.refreshed(): InventoryState {
        val trimmed = query.trim()
        val visible = allItems
            .filter { filter.matches(it) && (trimmed.isEmpty() || it.name.contains(trimmed, ignoreCase = true)) }
        val sorted = when (sort) {
            ItemSort.Name -> visible.sortedBy { it.name.lowercase() }
            ItemSort.Price -> visible.sortedByDescending { it.priceMinor }
            // Newest first; an item with no date has no place in a timeline and goes last — which
            // is what reversing a nulls-first comparator does.
            ItemSort.Acquired -> visible.sortedWith(
                compareByDescending<Item, LocalDate?>(nullsFirst()) {
                    it.acquiredOn
                },
            )
        }
        return copy(items = sorted)
    }

    companion object {

        const val ALERT_ID_DELETE = "delete_items"

        private const val KEY_CATEGORY = "filter_category"
        private const val KEY_TAGS = "filter_tags"
        private const val KEY_MAX_PRICE = "filter_max_price"
        private const val KEY_SORT = "sort"
        private const val SEPARATOR = ","
        private const val NO_CAP = -1L

        /** All the way up is no cap; anywhere else, whole units of the currency. */
        fun maxPriceFor(fraction: Float): Long? =
            if (fraction >= 1f) null else (fraction * InventoryState.PRICE_MAX_MINOR / 100).roundToLong() * 100

        fun fractionFor(maxPriceMinor: Long?): Float =
            if (maxPriceMinor ==
                null
            ) {
                1f
            } else {
                (maxPriceMinor.toFloat() / InventoryState.PRICE_MAX_MINOR).coerceIn(0f, 1f)
            }
    }
}
