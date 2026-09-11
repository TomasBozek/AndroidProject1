package com.example.androidproject1.feature.trips.presentation.tripwizard

import com.example.androidproject1.feature.trips.domain.TripType
import com.example.androidproject1.service.core.ui.event.UiEvent
import java.time.LocalDate

sealed interface TripWizardEvent : UiEvent {

    data class NameChanged(val name: String) : TripWizardEvent

    data class TypeSelected(val type: TripType) : TripWizardEvent

    data class StartDateChanged(val date: LocalDate) : TripWizardEvent

    data class EndDateChanged(val date: LocalDate) : TripWizardEvent

    data class TravelersChanged(val count: Int) : TripWizardEvent

    data object AdvancedToggled : TripWizardEvent

    data class NotesChanged(val notes: String) : TripWizardEvent

    data object PickDestinationClicked : TripWizardEvent

    data class BudgetRangeChanged(val range: ClosedFloatingPointRange<Float>) : TripWizardEvent

    /** Advances past the current step, or saves from the last one. */
    data object NextClicked : TripWizardEvent

    /** Steps back, or leaves the wizard from the first step. */
    data object NavigateUpClicked : TripWizardEvent

    /** The predictive back gesture completed while the first step held unsaved input. */
    data object BackRequested : TripWizardEvent
}
