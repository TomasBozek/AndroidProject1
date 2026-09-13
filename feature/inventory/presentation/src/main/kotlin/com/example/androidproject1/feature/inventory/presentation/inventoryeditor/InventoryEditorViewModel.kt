package com.example.androidproject1.feature.inventory.presentation.inventoryeditor

import com.example.androidproject1.core.ui.component.CheckState
import com.example.androidproject1.feature.inventory.domain.InventoryRepository
import com.example.androidproject1.feature.inventory.domain.ItemTag
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.ui.event.SystemEvent
import com.example.androidproject1.service.core.ui.form.ALERT_ID_DISCARD
import com.example.androidproject1.service.core.ui.form.discardAlert
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
            is InventoryEditorEvent.PriceChanged -> updateData {
                copy(priceMinor = InventoryEditorState.priceMinorFor(event.fraction))
            }
            is InventoryEditorEvent.InsuredChanged -> updateData { copy(insured = event.insured) }
            is InventoryEditorEvent.TagChanged -> updateData {
                copy(tags = if (event.checked) tags + event.tag else tags - event.tag)
            }
            // From "some", a tap on the master selects the rest — never clears what was chosen.
            is InventoryEditorEvent.AllTagsChanged -> updateData {
                val all = event.checked || allTagsState == CheckState.Indeterminate
                copy(tags = if (all) ItemTag.entries.toSet() else emptySet())
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
            updateData {
                if (item == null) copy(loading = false) else InventoryEditorState.forItem(item, step = step)
            }
        },
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

    // The id is the one the route carried, so an edit replaces the row rather than adding one.
    private fun save(state: InventoryEditorState) = execute(
        loading = overlay(),
        action = { inventoryRepository.saveItem(state.toItem()) },
        onData = { navigate(InventoryEditorNavigation.Saved) },
    )
}
