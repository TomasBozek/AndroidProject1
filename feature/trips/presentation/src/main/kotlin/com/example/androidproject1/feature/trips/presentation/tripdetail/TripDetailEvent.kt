package com.example.androidproject1.feature.trips.presentation.tripdetail

import com.example.androidproject1.service.core.ui.event.UiEvent

sealed interface TripDetailEvent : UiEvent {

    data class TabSelected(val index: Int) : TripDetailEvent

    data object DeleteClicked : TripDetailEvent

    /** The Up arrow was tapped. Every non-root screen has one. */
    data object NavigateUpClicked : TripDetailEvent
}
