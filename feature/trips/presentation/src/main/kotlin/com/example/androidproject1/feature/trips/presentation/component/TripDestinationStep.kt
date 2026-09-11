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
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppCard
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.ButtonKind
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.trips.presentation.R
import com.example.androidproject1.feature.trips.presentation.tripwizard.TripWizardEvent
import com.example.androidproject1.feature.trips.presentation.tripwizard.TripWizardState

/**
 * The wizard's second step: where the trip goes. Picking one leaves for `DestinationPickerScreen`
 * and comes back through the back stack, not a shared view model — see `TripWizardDestination`.
 */
@Composable
fun TripDestinationStep(
    state: TripWizardState,
    onEvent: (TripWizardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
    ) {
        if (state.destinationName != null) {
            AppCard(modifier = Modifier.testTag("tripWizard_destinationCard")) {
                AppText(text = state.destinationName, role = TextRole.Title)
            }
        } else {
            AppText(
                text = stringResource(R.string.trip_wizard_no_destination),
                role = TextRole.Secondary,
            )
        }

        AppButton(
            label = stringResource(
                if (state.destinationName != null) {
                    R.string.trip_wizard_change_destination
                } else {
                    R.string.trip_wizard_choose_destination
                },
            ),
            onClick = { onEvent(TripWizardEvent.PickDestinationClicked) },
            kind = ButtonKind.Outline,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tripWizard_chooseDestinationButton"),
        )
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    TripDestinationStep(
        state = TripWizardState.PREVIEW.copy(destinationId = "lisbon", destinationName = "Lisbon, Portugal"),
        onEvent = {},
    )
}
