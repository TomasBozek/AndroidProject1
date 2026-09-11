package com.example.androidproject1.feature.trips.presentation.destinationpicker

import com.example.androidproject1.feature.trips.domain.DestinationsRepository
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.service.core.ui.viewmodel.ErrorDisplay

class DestinationPickerViewModel(
    logger: Logger,
    // The route key, handed in by the destination. Available in `init`, and it comes back with
    // the back stack entry after process death.
    private val args: DestinationPickerDestination,
    private val destinationsRepository: DestinationsRepository,
) : BaseViewModel<DestinationPickerState, DestinationPickerEvent, DestinationPickerNavigation>(
    // The route key is already enough to draw with; the list fills in once it loads.
    initialState = DestinationPickerState(resultKey = args.resultKey),
    logger = logger.withTag("DestinationPickerViewModel"),
) {

    init {
        observeDestinations()
    }

    override fun onUiEvent(event: DestinationPickerEvent) = when (event) {
        is DestinationPickerEvent.DestinationClicked ->
            navigate(DestinationPickerNavigation.Picked(event.destination.id))

        DestinationPickerEvent.NavigateUpClicked -> navigate(DestinationPickerNavigation.NavigateUp)
    }

    private fun observeDestinations() = observe(
        flow = { destinationsRepository.observeDestinations() },
        errorDisplay = ErrorDisplay.Inline,
        onData = { destinations -> updateData { copy(destinations = destinations) } },
    )
}
