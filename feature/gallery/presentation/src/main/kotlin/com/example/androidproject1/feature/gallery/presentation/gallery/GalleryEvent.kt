package com.example.androidproject1.feature.gallery.presentation.gallery

import com.example.androidproject1.core.ui.event.UiEvent

sealed interface GalleryEvent : UiEvent {

    data class ComponentClicked(val id: String) : GalleryEvent

    /** The Up arrow was tapped. Every non-root screen has one. */
    data object NavigateUpClicked : GalleryEvent
}
