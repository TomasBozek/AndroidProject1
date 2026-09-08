package com.example.androidproject1.feature.gallery.presentation

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel

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
        }
    }
}
