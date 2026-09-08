package com.example.androidproject1.feature.gallery.presentation

sealed interface GalleryNavigation {

    data class ToComponent(val id: String) : GalleryNavigation
}
