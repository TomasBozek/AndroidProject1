package com.example.androidproject1.feature.gallery.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class GalleryDetailState(
    val componentId: String,
    val name: String,
    val group: String,
    val summary: String,
) {

    companion object {

        val PREVIEW = GalleryDetailState(
            componentId = "button",
            name = "AppButton",
            group = "Action",
            summary = "Six kinds, three sizes.",
        )
    }
}
