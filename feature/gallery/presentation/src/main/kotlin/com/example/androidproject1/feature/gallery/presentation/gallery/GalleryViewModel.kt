package com.example.androidproject1.feature.gallery.presentation.gallery

import com.example.androidproject1.feature.gallery.presentation.galleryItems
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel

class GalleryViewModel(
    logger: Logger,
) : BaseViewModel<GalleryState, GalleryEvent, GalleryNavigation>(
    // The catalogue is a compile-time constant, so the screen can draw on the first frame.
    initialState = GalleryState(items = galleryItems()),
    logger = logger.withTag("GalleryViewModel"),
) {

    override fun onUiEvent(event: GalleryEvent) {
        when (event) {
            is GalleryEvent.ComponentClicked -> navigate(GalleryNavigation.ToComponent(event.id))
            GalleryEvent.NavigateUpClicked -> navigate(GalleryNavigation.NavigateUp)
        }
    }
}
