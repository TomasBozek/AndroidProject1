package com.example.androidproject1.feature.trips.presentation.tripwizard

/** One-off navigation intents, turned into back-stack calls in TripWizardDestination. */
sealed interface TripWizardNavigation {

    /** Open the destination picker — the wizard's own result key travels with it. */
    data object PickDestination : TripWizardNavigation

    /** The trip was saved; leave the wizard. */
    data object Saved : TripWizardNavigation

    /** Left without saving. */
    data object NavigateUp : TripWizardNavigation
}
