package com.example.androidproject1.feature.gallery.presentation

import com.example.androidproject1.core.ui.event.UiEvent

sealed interface GalleryEvent : UiEvent {

    data class ComponentClicked(val id: String) : GalleryEvent
}
