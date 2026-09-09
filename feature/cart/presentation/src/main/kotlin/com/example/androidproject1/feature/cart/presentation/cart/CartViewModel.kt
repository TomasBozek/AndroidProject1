package com.example.androidproject1.feature.cart.presentation.cart

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.domain.result.Outcome
import com.example.androidproject1.core.ui.event.SystemEvent
import com.example.androidproject1.core.ui.state.setAlert
import com.example.androidproject1.core.ui.text.toUiText
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.core.ui.viewmodel.ErrorDisplay
import com.example.androidproject1.feature.cart.domain.CartItem
import com.example.androidproject1.feature.cart.domain.CartRepository
import com.example.androidproject1.feature.cart.presentation.R
import kotlinx.coroutines.flow.update

class CartViewModel(
    logger: Logger,
    private val cartRepository: CartRepository,
) : BaseViewModel<CartState, CartEvent, CartNavigation>(
    // The empty cart is a real state worth rendering, not an absence — so no overlay on first frame.
    initialState = CartState(),
    logger = logger.withTag("CartViewModel"),
) {

    companion object {

        const val ALERT_ID_CHECKOUT = "cart_checkout"
        const val SNACKBAR_UNDO_REMOVE = "cart_undo_remove"
    }

    /** What the last remove took away, so the snackbar action has something to put back. */
    private var lastRemoved: CartItem? = null

    init {
        observeItems()
    }

    override fun onUiEvent(event: CartEvent) {
        when (event) {
            is CartEvent.QuantityChanged ->
                write { cartRepository.setQuantity(event.productId, event.quantity) }

            is CartEvent.ItemRemoved -> removeItem(event.productId)

            CartEvent.AddItemClicked -> navigate(CartNavigation.PickProduct)

            CartEvent.CheckoutClicked -> confirmCheckout()
        }
    }

    override fun onSystemEvent(event: SystemEvent) {
        when {
            event is SystemEvent.SnackbarAction && event.id == SNACKBAR_UNDO_REMOVE -> {
                lastRemoved?.let { item -> write { cartRepository.add(item) } }
                lastRemoved = null
            }

            event is SystemEvent.AlertResult.Confirmed && event.id == ALERT_ID_CHECKOUT -> {
                super.onSystemEvent(event)
                checkout()
            }

            else -> super.onSystemEvent(event)
        }
    }

    /**
     * Adds a product picked in the catalog — the other half of `core.4`'s round trip.
     *
     * [lookup] is passed in rather than injected because it is the catalog's, and this module may
     * not depend on the catalog's presentation. It runs in `viewModelScope`, so an add survives
     * the screen that started it.
     */
    fun onProductPicked(productId: String, lookup: suspend (String) -> CartItem?) = execute(
        loading = {},
        action = {
            val item = lookup(productId)
            if (item != null) cartRepository.add(item) else Outcome.Success(Unit)
        },
        onData = {},
    )

    // loading = {}: the list is the screen, and an overlay over it on every quantity tap would
    // flash on each press of the stepper.
    private fun observeItems() = observe(
        flow = { cartRepository.observeItems() },
        loading = {},
        errorDisplay = ErrorDisplay.Inline,
        onData = { items -> uiState.update { it.copy(data = CartState(items = items)) } },
    )

    private fun removeItem(productId: String) {
        lastRemoved = uiState.value.data?.items?.find { it.productId == productId }
        write { cartRepository.remove(productId) }
        showSnackbar(
            message = R.string.cart_removed.toUiText(),
            actionLabel = R.string.cart_undo.toUiText(),
            id = SNACKBAR_UNDO_REMOVE,
        )
    }

    private fun confirmCheckout() {
        val state = uiState.value.data ?: return
        if (state.isEmpty) return
        // Confirm-then-act: the alert carries its own title, so an ordinary confirmation is not
        // labelled "Something went wrong" by BaseViewModel's error default.
        uiState.setAlert(
            id = ALERT_ID_CHECKOUT,
            title = R.string.cart_checkout_title.toUiText(),
            message = R.string.cart_checkout_message.toUiText(state.itemCountLabel),
            confirmLabel = R.string.cart_checkout_confirm.toUiText(),
            declineLabel = R.string.cart_checkout_cancel.toUiText(),
        )
    }

    private fun checkout() = execute(
        loading = {},
        action = { cartRepository.clear() },
        onData = { showToast(R.string.cart_checkout_done.toUiText()) },
    )

    /**
     * A write whose result the observed flow already reports.
     *
     * `onData` is empty on purpose: the list redraws because the database changed, not because
     * this call returned — two sources of truth for one row is how a cart starts disagreeing with
     * itself.
     */
    private fun write(action: suspend () -> Outcome<Unit>) =
        execute(loading = {}, action = action, onData = {})
}
