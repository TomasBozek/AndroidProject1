package com.example.androidproject1.feature.gallery.presentation

import androidx.compose.runtime.Immutable

/** One row of the gallery list. Plain data — the composable demo is looked up by [id]. */
@Immutable
data class GalleryItem(
    val id: String,
    val name: String,
    val group: String,
    val summary: String,
)

@Immutable
data class GalleryState(
    val items: List<GalleryItem>,
) {
    /** The list, in the order the design system groups them. */
    val groups: List<Pair<String, List<GalleryItem>>>
        get() = items.groupBy { it.group }.toList()

    companion object {

        val PREVIEW = GalleryState(items = galleryItems())
    }
}
