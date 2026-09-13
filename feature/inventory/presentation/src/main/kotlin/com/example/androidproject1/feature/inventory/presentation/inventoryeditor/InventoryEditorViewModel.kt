package com.example.androidproject1.feature.inventory.presentation.inventoryeditor

import com.example.androidproject1.feature.inventory.domain.InventoryRepository
import com.example.androidproject1.feature.inventory.domain.Item
import com.example.androidproject1.feature.inventory.domain.ItemTag
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.ui.event.SystemEvent
import com.example.androidproject1.service.core.ui.form.ALERT_ID_DISCARD
import com.example.androidproject1.service.core.ui.form.FieldState
import com.example.androidproject1.service.core.ui.form.discardAlert
import com.example.androidproject1.service.core.ui.form.required
import com.example.androidproject1.service.core.ui.state.clearAlert
import com.example.androidproject1.service.core.ui.state.setAlert
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.service.core.ui.viewmodel.ErrorDisplay

class InventoryEditorViewModel(
    logger: Logger,
    // The route key, handed in by the destination. Available in `init`, and it comes back with
    // the back stack entry after process death.
    private val args: InventoryEditorDestination,
    private val inventoryRepository: InventoryRepository,
) : BaseViewModel<InventoryEditorState, InventoryEditorEvent, InventoryEditorNavigation>(
    // The form draws at once; if the id names an item, its fields arrive over the blank ones.
    initialState = InventoryEditorState(itemId = args.itemId, loading = true),
    logger = logger.withTag("InventoryEditorViewModel"),
) {

    init {
        load()
    }

    override fun onSystemEvent(event: SystemEvent) {
        if (event is SystemEvent.AlertResult.Confirmed && event.id == ALERT_ID_DISCARD) {
            uiState.clearAlert()
            navigate(InventoryEditorNavigation.NavigateUp)
            return
        }
        super.onSystemEvent(event)
    }

    override fun onUiEvent(event: InventoryEditorEvent) {
        when (event) {
            is InventoryEditorEvent.NameChanged -> updateData { copy(name = name.changed(event.name)) }
            is InventoryEditorEvent.CategorySelected -> updateData { copy(category = event.category) }
            is InventoryEditorEvent.ConditionSelected -> updateData { copy(condition = event.condition) }
            is InventoryEditorEvent.AcquiredOnChanged -> updateData { copy(acquiredOn = event.date) }
            is InventoryEditorEvent.QuantityChanged -> updateData { copy(quantity = event.quantity) }
            is InventoryEditorEvent.PriceChanged -> updateData { copy(priceFraction = event.fraction) }
            is InventoryEditorEvent.InsuredChanged -> updateData { copy(insured = event.insured) }
            is InventoryEditorEvent.TagChanged -> updateData {
                copy(tags = if (event.checked) tags + event.tag else tags - event.tag)
            }
            is InventoryEditorEvent.AllTagsChanged -> updateData {
                copy(tags = if (event.checked) ItemTag.entries.toSet() else emptySet())
            }
            is InventoryEditorEvent.OwnerSelected -> updateData { copy(owner = event.owner) }
            is InventoryEditorEvent.ImageUrlChanged -> updateData { copy(imageUrl = event.imageUrl) }
            is InventoryEditorEvent.NotesChanged -> updateData { copy(notes = event.notes) }
            InventoryEditorEvent.NextClicked -> next()
            InventoryEditorEvent.NavigateUpClicked -> back()
            InventoryEditorEvent.BackRequested -> uiState.setAlert(discardAlert())
        }
    }

    // Silent: an id that names nothing is the "new item" case, not a failure — and a read error
    // leaves a blank form, which is still a form.
    private fun load() = execute(
        errorDisplay = ErrorDisplay.Silent,
        action = { inventoryRepository.getItem(args.itemId) },
        onData = { item ->
            updateData { if (item == null) copy(loading = false) else fromItem(item) }
        },
    )

    private fun InventoryEditorState.fromItem(item: Item) = copy(
        loading = false,
        editing = true,
        name = FieldState.of(required(), value = item.name),
        category = item.category,
        condition = item.condition,
        acquiredOn = item.acquiredOn,
        quantity = item.quantity,
        priceFraction = InventoryEditorState.fractionFor(item.priceMinor),
        insured = item.insured,
        tags = item.tags,
        owner = item.owner,
        imageUrl = item.imageUrl.orEmpty(),
        notes = item.notes,
    )

    private fun next() {
        val state = uiState.value.data ?: return
        if (!state.canContinue) {
            // A required field the user has not touched shows its error from here on.
            updateData { copy(name = name.touch()) }
            return
        }
        if (state.step == InventoryEditorState.STEP_REVIEW) {
            save(state)
        } else {
            updateData { copy(step = step + 1) }
        }
    }

    private fun back() {
        val state = uiState.value.data ?: return
        if (state.step > InventoryEditorState.STEP_BASICS) {
            updateData { copy(step = step - 1) }
        } else {
            navigate(InventoryEditorNavigation.NavigateUp)
        }
    }

    private fun save(state: InventoryEditorState) = execute(
        loading = overlay(),
        action = {
            inventoryRepository.saveItem(
                Item(
                    id = state.itemId,
                    name = state.name.value.trim(),
                    category = state.category,
                    condition = state.condition,
                    quantity = state.quantity,
                    priceMinor = state.priceMinor,
                    acquiredOn = state.acquiredOn,
                    insured = state.insured,
                    tags = state.tags,
                    owner = state.owner,
                    imageUrl = state.imageUrl.trim().ifBlank { null },
                    notes = state.notes.trim(),
                ),
            )
        },
        onData = { navigate(InventoryEditorNavigation.Saved) },
    )
}
