package com.example.androidproject1.feature.trips.presentation.tripdetail

import com.example.androidproject1.feature.trips.domain.TripsRepository
import com.example.androidproject1.feature.trips.presentation.R
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.ui.event.SystemEvent
import com.example.androidproject1.service.core.ui.state.clearAlert
import com.example.androidproject1.service.core.ui.state.setAlert
import com.example.androidproject1.service.core.ui.text.toUiText
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.service.core.ui.viewmodel.ErrorDisplay
import java.time.Clock
import java.time.LocalDate

class TripDetailViewModel(
    logger: Logger,
    // The route key, handed in by the destination. Available in `init`, and it comes back with
    // the back stack entry after process death.
    private val args: TripDetailDestination,
    private val tripsRepository: TripsRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : BaseViewModel<TripDetailState, TripDetailEvent, TripDetailNavigation>(
    // The route key is already enough to draw with; the trip itself fills in once it loads.
    initialState = TripDetailState(tripId = args.tripId, today = LocalDate.now(clock)),
    logger = logger.withTag("TripDetailViewModel"),
) {

    init {
        loadTrip()
    }

    override fun onSystemEvent(event: SystemEvent) {
        if (event is SystemEvent.AlertResult.Confirmed && event.id == ALERT_ID_DELETE) {
            uiState.clearAlert()
            deleteTrip()
            return
        }
        super.onSystemEvent(event)
    }

    override fun onUiEvent(event: TripDetailEvent) {
        when (event) {
            is TripDetailEvent.TabSelected -> updateData { copy(selectedTab = event.index) }

            TripDetailEvent.DeleteClicked -> uiState.setAlert(
                id = ALERT_ID_DELETE,
                title = R.string.trip_detail_delete_title.toUiText(),
                message = R.string.trip_detail_delete_message.toUiText(),
                confirmLabel = R.string.trip_detail_delete_confirm.toUiText(),
                declineLabel = R.string.trip_detail_delete_cancel.toUiText(),
            )

            TripDetailEvent.NavigateUpClicked -> navigate(TripDetailNavigation.NavigateUp)
        }
    }

    private fun loadTrip() = execute(
        loading = overlay(),
        errorDisplay = ErrorDisplay.Inline,
        action = { tripsRepository.getTrip(args.tripId) },
        onData = { trip ->
            if (trip != null) {
                updateData { copy(trip = trip) }
            } else {
                // Gone — deleted from another screen, or a stale link. Nothing left to show here.
                navigate(TripDetailNavigation.NavigateUp)
            }
        },
    )

    private fun deleteTrip() = execute(
        loading = overlay(),
        action = { tripsRepository.deleteTrip(args.tripId) },
        onData = { navigate(TripDetailNavigation.NavigateUp) },
    )

    private companion object {

        const val ALERT_ID_DELETE = "delete_trip"
    }
}
