package com.example.androidproject1.feature.trips.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.component.AppAccordion
import com.example.androidproject1.core.ui.component.AppDateField
import com.example.androidproject1.core.ui.component.AppSegmented
import com.example.androidproject1.core.ui.component.AppStepper
import com.example.androidproject1.core.ui.component.AppTextField
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.trips.domain.TripType
import com.example.androidproject1.feature.trips.presentation.R
import com.example.androidproject1.feature.trips.presentation.tripwizard.TripWizardEvent
import com.example.androidproject1.feature.trips.presentation.tripwizard.TripWizardState

/**
 * The wizard's first step: what the trip is called, what kind it is, when it runs and who is
 * coming. Its own file because `TripWizardScreen` composes three of these and a screen file holds
 * only the screen and its previews.
 */
@Composable
fun TripDetailsStep(
    state: TripWizardState,
    onEvent: (TripWizardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
    ) {
        AppTextField(
            value = state.name,
            onValueChange = { onEvent(TripWizardEvent.NameChanged(it)) },
            label = stringResource(R.string.trip_wizard_name_label),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tripWizard_nameField"),
        )

        val types = TripType.entries
        AppSegmented(
            options = types.map { stringResource(it.labelRes()) },
            selectedIndex = types.indexOf(state.type),
            onSelect = { onEvent(TripWizardEvent.TypeSelected(types[it])) },
            modifier = Modifier.testTag("tripWizard_typeSegmented"),
        )

        AppDateField(
            value = state.startDate,
            onValueChange = { onEvent(TripWizardEvent.StartDateChanged(it)) },
            label = stringResource(R.string.trip_wizard_start_date_label),
            modifier = Modifier.testTag("tripWizard_startDateField"),
        )

        AppDateField(
            value = state.endDate,
            onValueChange = { onEvent(TripWizardEvent.EndDateChanged(it)) },
            label = stringResource(R.string.trip_wizard_end_date_label),
            modifier = Modifier.testTag("tripWizard_endDateField"),
        )

        AppStepper(
            value = state.travelers,
            onValueChange = { onEvent(TripWizardEvent.TravelersChanged(it)) },
            min = 1,
            modifier = Modifier.testTag("tripWizard_travelersStepper"),
        )

        AppAccordion(
            title = stringResource(R.string.trip_wizard_advanced_label),
            expanded = state.advancedExpanded,
            onToggle = { onEvent(TripWizardEvent.AdvancedToggled) },
            modifier = Modifier.testTag("tripWizard_advancedAccordion"),
        ) {
            AppTextField(
                value = state.notes,
                onValueChange = { onEvent(TripWizardEvent.NotesChanged(it)) },
                label = stringResource(R.string.trip_wizard_notes_label),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tripWizard_notesField"),
            )
        }
    }
}

private fun TripType.labelRes() = when (this) {
    TripType.Leisure -> R.string.trip_wizard_type_leisure
    TripType.Business -> R.string.trip_wizard_type_business
    TripType.Family -> R.string.trip_wizard_type_family
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    TripDetailsStep(state = TripWizardState.PREVIEW, onEvent = {})
}
