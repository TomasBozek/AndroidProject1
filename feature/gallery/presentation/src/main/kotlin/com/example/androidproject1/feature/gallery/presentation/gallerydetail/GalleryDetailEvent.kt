package com.example.androidproject1.feature.gallery.presentation.gallerydetail

import com.example.androidproject1.core.ui.event.UiEvent

sealed interface GalleryDetailEvent : UiEvent {

    data object NavigateUpClicked : GalleryDetailEvent
}
