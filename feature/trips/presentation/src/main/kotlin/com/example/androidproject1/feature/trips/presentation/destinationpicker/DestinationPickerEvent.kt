package com.example.androidproject1.feature.trips.presentation.destinationpicker

import com.example.androidproject1.feature.trips.domain.Destination
import com.example.androidproject1.service.core.ui.event.UiEvent

sealed interface DestinationPickerEvent : UiEvent {

    data class DestinationClicked(val destination: Destination) : DestinationPickerEvent

    /** The Up arrow was tapped. Every non-root screen has one. */
    data object NavigateUpClicked : DestinationPickerEvent
}
