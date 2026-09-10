package com.example.androidproject1.feature.gallery.presentation.gallerydetail

import com.example.androidproject1.feature.gallery.presentation.galleryEntry
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel

class GalleryDetailViewModel(
    logger: Logger,
    // The route key, handed in by the destination. Available in `init`, and it comes back with
    // the back stack entry after process death.
    private val args: GalleryDetailDestination,
) : BaseViewModel<GalleryDetailState, GalleryDetailEvent, GalleryDetailNavigation>(
    initialState = galleryEntry(args.componentId)?.let {
        GalleryDetailState(
            componentId = it.id,
            name = it.name,
            group = it.group,
            summary = it.summary,
        )
    },
    logger = logger.withTag("GalleryDetailViewModel"),
) {

    override fun onUiEvent(event: GalleryDetailEvent) {
        when (event) {
            GalleryDetailEvent.NavigateUpClicked -> navigate(GalleryDetailNavigation.Up)
        }
    }
}
