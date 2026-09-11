package com.example.androidproject1.feature.trips.presentation.trips

import com.example.androidproject1.service.core.ui.event.UiEvent

sealed interface TripsEvent : UiEvent {

    data object ViewAllClicked : TripsEvent

    data object NewTripClicked : TripsEvent

    data object NextTripClicked : TripsEvent
}
