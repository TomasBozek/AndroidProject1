package com.example.androidproject1.feature.gallery.presentation.gallery

sealed interface GalleryNavigation {

    data class ToComponent(val id: String) : GalleryNavigation

    data object NavigateUp : GalleryNavigation
}
