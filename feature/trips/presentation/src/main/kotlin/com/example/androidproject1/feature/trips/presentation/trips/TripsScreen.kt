package com.example.androidproject1.feature.trips.presentation.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppCard
import com.example.androidproject1.core.ui.component.AppEmptyState
import com.example.androidproject1.core.ui.component.AppProgress
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppSectionHeader
import com.example.androidproject1.core.ui.component.AppSkeleton
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.component.ButtonKind
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.trips.presentation.R
import com.example.androidproject1.feature.trips.presentation.component.TripStatusDot
import com.example.androidproject1.service.core.ui.format.LocalFormats

@Composable
fun TripsScreen(
    state: TripsState,
    onEvent: (TripsEvent) -> Unit,
) {
    AppScaffold(
        // No up arrow: this is a tab root, and the bottom bar is what leaves it.
        screenId = "TripsScreen",
        topBar = { AppTopBar(title = stringResource(R.string.trips_title)) },
    ) {
        if (state.loading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("trips_skeleton"),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.sm),
            ) {
                repeat(SKELETON_ROWS) { AppSkeleton(height = AppTheme.density.listRowHeight) }
            }
        } else {
            val formats = LocalFormats.current
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
            ) {
                AppSectionHeader(
                    title = stringResource(R.string.trips_section_header),
                    actionLabel = stringResource(R.string.trips_view_all),
                    onAction = { onEvent(TripsEvent.ViewAllClicked) },
                )

                AppText(
                    text = stringResource(R.string.trips_count_label),
                    role = TextRole.Label,
                )
                AppText(
                    text = state.tripCount.toString(),
                    role = TextRole.Numeric,
                    modifier = Modifier.testTag("trips_countValue"),
                )

                val nextTrip = state.nextTrip
                if (nextTrip != null) {
                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trips_nextTripCard"),
                    ) {
                        AppText(text = nextTrip.name, role = TextRole.Title)
                        AppText(
                            text = "${nextTrip.destinationName} · ${formats.date(nextTrip.startDate)}",
                            role = TextRole.Secondary,
                        )
                        TripStatusDot(status = nextTrip.status(state.today))
                        AppProgress(
                            fraction = state.countdownFraction,
                            label = stringResource(R.string.trips_countdown_label),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = AppTheme.spacing.stack.sm),
                        )
                        AppButton(
                            label = stringResource(R.string.trips_open_next_trip),
                            onClick = { onEvent(TripsEvent.NextTripClicked) },
                            kind = ButtonKind.Outline,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = AppTheme.spacing.stack.sm)
                                .testTag("trips_openNextTripButton"),
                        )
                    }
                    // A second, more prominent way to start another one — the card above already
                    // opens the one that exists.
                    AppButton(
                        label = stringResource(R.string.trips_new_trip),
                        onClick = { onEvent(TripsEvent.NewTripClicked) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trips_newTripButton"),
                    )
                } else {
                    // The empty state's own action is the only "new trip" entry here — a second
                    // button under it would be the same offer twice.
                    AppEmptyState(
                        title = stringResource(R.string.trips_empty_title),
                        message = stringResource(R.string.trips_empty_message),
                        actionLabel = stringResource(R.string.trips_new_trip),
                        onAction = { onEvent(TripsEvent.NewTripClicked) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trips_emptyState"),
                    )
                }
            }
        }
    }
}

private const val SKELETON_ROWS = 4

@ScreenPreview
@Composable
private fun Preview(
    @PreviewParameter(TripsStatePreviews::class) state: TripsState,
) = ThemedScreenPreview {
    TripsScreen(state = state) {}
}
