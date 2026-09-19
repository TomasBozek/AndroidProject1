package com.example.androidproject1.feature.home.presentation.home

import com.example.androidproject1.service.core.ui.event.UiEvent

sealed interface HomeEvent : UiEvent {

    /** The remove button on a favourite was tapped. The undo lives in the ViewModel. */
    data class FavouriteRemoved(val productId: String) : HomeEvent

    data object InventoryClicked : HomeEvent

    data object MoviesClicked : HomeEvent
}
