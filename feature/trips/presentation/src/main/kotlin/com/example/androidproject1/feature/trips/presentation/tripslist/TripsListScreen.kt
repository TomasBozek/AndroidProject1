package com.example.androidproject1.feature.trips.presentation.tripslist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppEmptyState
import com.example.androidproject1.core.ui.component.AppFab
import com.example.androidproject1.core.ui.component.AppListItem
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppScrollShadow
import com.example.androidproject1.core.ui.component.AppSkeleton
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.component.ScrollEdge
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.trips.presentation.R
import com.example.androidproject1.feature.trips.presentation.component.TripStatusDot
import com.example.androidproject1.service.core.ui.format.LocalFormats

@Composable
fun TripsListScreen(
    state: TripsListState,
    onEvent: (TripsListEvent) -> Unit,
) {
    AppScaffold(
        screenId = "TripsListScreen",
        topBar = {
            AppTopBar(
                title = stringResource(R.string.trips_list_title),
                onNavigateUp = { onEvent(TripsListEvent.NavigateUpClicked) },
                navigateUpTestTag = "tripsList_upButton",
            )
        },
        contentPadding = false,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.loading) {
                // The shell is already drawn; the rows are what is still loading.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AppTheme.spacing.inset.xl)
                        .testTag("tripsList_skeleton"),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.sm),
                ) {
                    repeat(SKELETON_ROWS) { AppSkeleton(height = AppTheme.density.listRowHeight) }
                }
            } else if (state.trips.isEmpty()) {
                // Inside the scaffold, so the up arrow and the screen id stay: a ContentState would
                // stand in for the whole screen (E0X1).
                AppEmptyState(
                    title = stringResource(R.string.trips_list_empty_title),
                    message = stringResource(R.string.trips_list_empty),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AppTheme.spacing.inset.xl)
                        .testTag("tripsList_empty"),
                )
            } else {
                val formats = LocalFormats.current
                val scroll = rememberLazyListState()
                LazyColumn(
                    state = scroll,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("tripsList_list"),
                ) {
                    items(items = state.trips, key = { it.id }) { trip ->
                        AppListItem(
                            headline = trip.name,
                            supporting = "${trip.destinationName} · ${formats.date(trip.startDate)}",
                            onClick = { onEvent(TripsListEvent.TripClicked(trip)) },
                            trailing = { TripStatusDot(status = trip.status(state.today)) },
                            modifier = Modifier.testTag("tripsList_item"),
                        )
                    }
                }
                // The cue that the list carries on past the fold — present only while there is more.
                AppScrollShadow(scroll, ScrollEdge.Top, Modifier.align(Alignment.TopCenter))
                AppScrollShadow(scroll, ScrollEdge.Bottom, Modifier.align(Alignment.BottomCenter))
            }

            AppFab(
                icon = Icons.Filled.Add,
                contentDescription = stringResource(R.string.trips_list_new_trip),
                onClick = { onEvent(TripsListEvent.NewTripClicked) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(AppTheme.spacing.inset.lg)
                    .testTag("tripsList_newTripButton"),
            )
        }
    }
}

private const val SKELETON_ROWS = 4

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    TripsListScreen(state = TripsListState.PREVIEW) {}
}

@ScreenPreview
@Composable
private fun LoadingPreview() = ThemedScreenPreview {
    TripsListScreen(state = TripsListState.PREVIEW.copy(loading = true)) {}
}

@ScreenPreview
@Composable
private fun EmptyPreview() = ThemedScreenPreview {
    TripsListScreen(state = TripsListState.PREVIEW.copy(trips = emptyList())) {}
}
