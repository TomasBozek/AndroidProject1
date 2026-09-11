package com.example.androidproject1.feature.trips.presentation.tripwizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppStepProgress
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.trips.presentation.R
import com.example.androidproject1.feature.trips.presentation.component.TripDestinationStep
import com.example.androidproject1.feature.trips.presentation.component.TripDetailsStep
import com.example.androidproject1.feature.trips.presentation.component.TripReviewStep
import com.example.androidproject1.service.core.ui.form.DiscardBackHandler

@Composable
fun TripWizardScreen(
    state: TripWizardState,
    onEvent: (TripWizardEvent) -> Unit,
) {
    AppScaffold(
        screenId = "TripWizardScreen",
        topBar = {
            AppTopBar(
                title = stringResource(R.string.trip_wizard_title),
                onNavigateUp = { onEvent(TripWizardEvent.NavigateUpClicked) },
                navigateUpTestTag = "tripWizard_upButton",
            )
        },
    ) {
        // Only the first step guards the gesture: past it, leaving pops the whole wizard the way
        // the up arrow does too — see TripWizardViewModel.back().
        DiscardBackHandler(dirty = state.step == TripWizardState.STEP_DETAILS && state.isDirty) {
            onEvent(TripWizardEvent.BackRequested)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.lg),
        ) {
            AppStepProgress(
                steps = TripWizardState.STEP_COUNT,
                currentStep = state.step,
                label = stringResource(
                    R.string.trip_wizard_step_label,
                    state.step + 1,
                    TripWizardState.STEP_COUNT,
                ),
                modifier = Modifier.testTag("tripWizard_stepProgress"),
            )

            when (state.step) {
                TripWizardState.STEP_DESTINATION -> TripDestinationStep(state = state, onEvent = onEvent)
                TripWizardState.STEP_REVIEW -> TripReviewStep(state = state, onEvent = onEvent)
                else -> TripDetailsStep(state = state, onEvent = onEvent)
            }

            AppButton(
                label = stringResource(
                    if (state.step == TripWizardState.STEP_REVIEW) {
                        R.string.trip_wizard_save
                    } else {
                        R.string.trip_wizard_next
                    },
                ),
                onClick = { onEvent(TripWizardEvent.NextClicked) },
                enabled = state.canContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tripWizard_nextButton"),
            )
        }
    }
}

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    TripWizardScreen(state = TripWizardState.PREVIEW) {}
}

@ScreenPreview
@Composable
private fun DestinationStepPreview() = ThemedScreenPreview {
    TripWizardScreen(state = TripWizardState.PREVIEW.copy(step = TripWizardState.STEP_DESTINATION)) {}
}

@ScreenPreview
@Composable
private fun ReviewStepPreview() = ThemedScreenPreview {
    TripWizardScreen(
        state = TripWizardState.PREVIEW.copy(
            step = TripWizardState.STEP_REVIEW,
            destinationId = "lisbon",
            destinationName = "Lisbon, Portugal",
        ),
    ) {}
}
