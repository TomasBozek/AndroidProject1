package com.example.androidproject1.feature.trips.presentation.destinationpicker

/** One-off navigation intents, turned into back-stack calls in DestinationPickerDestination. */
sealed interface DestinationPickerNavigation {

    /**
     * The user chose [destinationId]; hand it back to whoever asked and close.
     *
     * The picker does not know who asked — the requester's key came in as a route argument — which
     * is what lets one picker serve the wizard today and anything else tomorrow.
     */
    data class Picked(val destinationId: String) : DestinationPickerNavigation

    /** Left without picking. The requester gets no result, which is the answer. */
    data object NavigateUp : DestinationPickerNavigation
}
