package com.example.androidproject1.feature.trips.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.component.AppDescriptionList
import com.example.androidproject1.core.ui.component.AppRangeSlider
import com.example.androidproject1.core.ui.component.AppTooltip
import com.example.androidproject1.core.ui.component.DescriptionRow
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.trips.presentation.R
import com.example.androidproject1.feature.trips.presentation.tripwizard.TripWizardEvent
import com.example.androidproject1.feature.trips.presentation.tripwizard.TripWizardState
import com.example.androidproject1.service.core.ui.format.LocalFormats

/** The wizard's last step: the budget range, and everything chosen so far to look over before saving. */
@Composable
fun TripReviewStep(
    state: TripWizardState,
    onEvent: (TripWizardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val formats = LocalFormats.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppRangeSlider(
                value = state.budgetRange,
                onValueChange = { onEvent(TripWizardEvent.BudgetRangeChanged(it)) },
                label = stringResource(R.string.trip_wizard_budget_label),
                valueLabel = "${formats.moneyShort(TripWizardState.budgetMinorFor(state.budgetRange.start))} – " +
                    formats.moneyShort(TripWizardState.budgetMinorFor(state.budgetRange.endInclusive)),
                modifier = Modifier
                    .weight(1f)
                    .testTag("tripWizard_budgetSlider"),
            )
        }
        AppTooltip(text = stringResource(R.string.trip_wizard_budget_tooltip))

        AppDescriptionList(
            rows = listOf(
                DescriptionRow(stringResource(R.string.trip_detail_destination_label), state.destinationName),
                DescriptionRow(
                    stringResource(R.string.trip_detail_dates_label),
                    if (state.startDate != null && state.endDate != null) {
                        "${formats.date(state.startDate)} – ${formats.date(state.endDate)}"
                    } else {
                        null
                    },
                ),
                DescriptionRow(
                    stringResource(R.string.trip_detail_travelers_label),
                    state.travelers.toString(),
                    numeric = true,
                ),
            ),
            modifier = Modifier.testTag("tripWizard_reviewSummary"),
        )
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    TripReviewStep(
        state = TripWizardState.PREVIEW.copy(destinationId = "lisbon", destinationName = "Lisbon, Portugal"),
        onEvent = {},
    )
}
