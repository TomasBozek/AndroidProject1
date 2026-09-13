package com.example.androidproject1.feature.inventory.presentation.inventorydetail

import com.example.androidproject1.feature.inventory.domain.InventoryRepository
import com.example.androidproject1.feature.inventory.presentation.R
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.ui.event.SystemEvent
import com.example.androidproject1.service.core.ui.state.AlertPayload
import com.example.androidproject1.service.core.ui.state.clearAlert
import com.example.androidproject1.service.core.ui.state.setAlert
import com.example.androidproject1.service.core.ui.text.toUiText
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.service.core.ui.viewmodel.ErrorDisplay

class InventoryDetailViewModel(
    logger: Logger,
    // The route key, handed in by the destination. Available in `init`, and it comes back with
    // the back stack entry after process death.
    private val args: InventoryDetailDestination,
    private val inventoryRepository: InventoryRepository,
) : BaseViewModel<InventoryDetailState, InventoryDetailEvent, InventoryDetailNavigation>(
    // The route key is already enough to draw with; the item fills in once it arrives.
    initialState = InventoryDetailState(itemId = args.itemId),
    logger = logger.withTag("InventoryDetailViewModel"),
) {

    /**
     * What the delete alert carries back with its result. A typed payload rather than the alert
     * id alone: the confirm handler reads the id it is deleting off the event, which is the shape
     * a screen confirming one of several rows needs — and the first real user of `AlertPayload`.
     */
    private data class DeleteItem(val itemId: String) : AlertPayload

    /**
     * Set once this screen has decided to go. A confirmed delete pops the screen, and the observed
     * item then emits `null` for the same deletion — two reasons to leave, one pop: the second
     * would take the list below with it, which is what a first run of the flow did.
     */
    private var leaving = false

    init {
        observeItem()
    }

    override fun onSystemEvent(event: SystemEvent) {
        if (event is SystemEvent.AlertResult.Confirmed && event.id == ALERT_ID_DELETE) {
            uiState.clearAlert()
            (event.payload as? DeleteItem)?.let { delete(it.itemId) }
            return
        }
        super.onSystemEvent(event)
    }

    override fun onUiEvent(event: InventoryDetailEvent) {
        when (event) {
            is InventoryDetailEvent.SectionSelected -> updateData { copy(selectedSection = event.index) }
            InventoryDetailEvent.EditClicked -> navigate(InventoryDetailNavigation.Edit(args.itemId))
            InventoryDetailEvent.DeleteClicked -> uiState.setAlert(
                id = ALERT_ID_DELETE,
                title = R.string.inventory_detail_delete_title.toUiText(),
                message = R.string.inventory_detail_delete_message.toUiText(),
                confirmLabel = R.string.inventory_detail_delete_confirm.toUiText(),
                declineLabel = R.string.inventory_detail_delete_cancel.toUiText(),
                payload = DeleteItem(args.itemId),
            )
            InventoryDetailEvent.NavigateUpClicked -> navigate(InventoryDetailNavigation.NavigateUp)
        }
    }

    // Observed rather than read once, so an edit saved on the screen above shows up here without
    // a reload. Inline: a read error is content, not a dialog over a shell with nothing in it.
    private fun observeItem() = observe(
        flow = { inventoryRepository.observeItem(args.itemId) },
        errorDisplay = ErrorDisplay.Inline,
        onData = { item ->
            if (item != null) {
                updateData { copy(item = item) }
            } else {
                // Gone — deleted here or from another screen, or a stale link. Nothing left to show.
                leave()
            }
        },
    )

    private fun delete(itemId: String) = execute(
        loading = overlay(),
        action = { inventoryRepository.deleteItems(setOf(itemId)) },
        onData = { leave() },
    )

    private fun leave() {
        if (leaving) return
        leaving = true
        navigate(InventoryDetailNavigation.NavigateUp)
    }

    private companion object {

        const val ALERT_ID_DELETE = "delete_item"
    }
}
