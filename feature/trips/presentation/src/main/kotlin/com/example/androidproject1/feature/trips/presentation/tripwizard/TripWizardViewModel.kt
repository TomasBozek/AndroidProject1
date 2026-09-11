package com.example.androidproject1.feature.trips.presentation.tripwizard

import com.example.androidproject1.feature.trips.domain.DestinationsRepository
import com.example.androidproject1.feature.trips.domain.Trip
import com.example.androidproject1.feature.trips.domain.TripsRepository
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.ui.event.SystemEvent
import com.example.androidproject1.service.core.ui.form.ALERT_ID_DISCARD
import com.example.androidproject1.service.core.ui.form.discardAlert
import com.example.androidproject1.service.core.ui.state.clearAlert
import com.example.androidproject1.service.core.ui.state.setAlert
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.service.core.ui.viewmodel.ErrorDisplay
import java.util.UUID

class TripWizardViewModel(
    logger: Logger,
    private val tripsRepository: TripsRepository,
    private val destinationsRepository: DestinationsRepository,
) : BaseViewModel<TripWizardState, TripWizardEvent, TripWizardNavigation>(
    // The wizard has nothing to load — it draws from an empty form immediately.
    initialState = TripWizardState(),
    logger = logger.withTag("TripWizardViewModel"),
) {

    override fun onSystemEvent(event: SystemEvent) {
        if (event is SystemEvent.AlertResult.Confirmed && event.id == ALERT_ID_DISCARD) {
            uiState.clearAlert()
            navigate(TripWizardNavigation.NavigateUp)
            return
        }
        super.onSystemEvent(event)
    }

    override fun onUiEvent(event: TripWizardEvent) {
        when (event) {
            is TripWizardEvent.NameChanged -> updateData { copy(name = event.name) }
            is TripWizardEvent.TypeSelected -> updateData { copy(type = event.type) }
            is TripWizardEvent.StartDateChanged -> updateData { copy(startDate = event.date) }
            is TripWizardEvent.EndDateChanged -> updateData { copy(endDate = event.date) }
            is TripWizardEvent.TravelersChanged -> updateData { copy(travelers = event.count) }
            TripWizardEvent.AdvancedToggled -> updateData { copy(advancedExpanded = !advancedExpanded) }
            is TripWizardEvent.NotesChanged -> updateData { copy(notes = event.notes) }
            is TripWizardEvent.BudgetRangeChanged -> updateData { copy(budgetRange = event.range) }
            TripWizardEvent.PickDestinationClicked -> navigate(TripWizardNavigation.PickDestination)
            TripWizardEvent.NextClicked -> next()
            TripWizardEvent.NavigateUpClicked -> back()
            TripWizardEvent.BackRequested -> uiState.setAlert(discardAlert())
        }
    }

    /** The picker's result — resolved to a name here, since the picker hands back only an id. */
    fun onDestinationPicked(destinationId: String) = execute(
        errorDisplay = ErrorDisplay.Silent,
        action = { destinationsRepository.getDestination(destinationId) },
        onData = { destination ->
            if (destination != null) {
                updateData { copy(destinationId = destination.id, destinationName = destination.name) }
            }
        },
    )

    private fun next() {
        val state = uiState.value.data ?: return
        if (!state.canContinue) return
        if (state.step == TripWizardState.STEP_REVIEW) {
            save()
        } else {
            updateData { copy(step = step + 1) }
        }
    }

    private fun back() {
        val state = uiState.value.data ?: return
        if (state.step > TripWizardState.STEP_DETAILS) {
            updateData { copy(step = step - 1) }
        } else {
            navigate(TripWizardNavigation.NavigateUp)
        }
    }

    private fun save() {
        val state = uiState.value.data ?: return
        val startDate = state.startDate ?: return
        val endDate = state.endDate ?: return
        val destinationId = state.destinationId ?: return

        execute(
            loading = overlay(),
            action = {
                tripsRepository.saveTrip(
                    Trip(
                        id = UUID.randomUUID().toString(),
                        name = state.name.trim(),
                        destinationId = destinationId,
                        destinationName = state.destinationName.orEmpty(),
                        type = state.type,
                        startDate = startDate,
                        endDate = endDate,
                        travelers = state.travelers,
                        budgetMinMinor = TripWizardState.budgetMinorFor(state.budgetRange.start),
                        budgetMaxMinor = TripWizardState.budgetMinorFor(state.budgetRange.endInclusive),
                        notes = state.notes,
                    ),
                )
            },
            onData = { navigate(TripWizardNavigation.Saved) },
        )
    }
}
