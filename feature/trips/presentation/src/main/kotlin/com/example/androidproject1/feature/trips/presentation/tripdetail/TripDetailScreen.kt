package com.example.androidproject1.feature.trips.presentation.tripdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppDescriptionList
import com.example.androidproject1.core.ui.component.AppFieldGroup
import com.example.androidproject1.core.ui.component.AppFieldGroupRow
import com.example.androidproject1.core.ui.component.AppIconButton
import com.example.androidproject1.core.ui.component.AppMenu
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppSkeleton
import com.example.androidproject1.core.ui.component.AppTabs
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.component.DescriptionRow
import com.example.androidproject1.core.ui.component.MenuItem
import com.example.androidproject1.core.ui.component.TabItem
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.trips.presentation.R
import com.example.androidproject1.feature.trips.presentation.component.TripStatusDot
import com.example.androidproject1.service.core.ui.format.LocalFormats

@Composable
fun TripDetailScreen(
    state: TripDetailState,
    onEvent: (TripDetailEvent) -> Unit,
) {
    val trip = state.trip
    AppScaffold(
        screenId = "TripDetailScreen",
        topBar = {
            AppTopBar(
                title = trip?.name ?: stringResource(R.string.trip_detail_title),
                onNavigateUp = { onEvent(TripDetailEvent.NavigateUpClicked) },
                navigateUpTestTag = "tripDetail_upButton",
                actions = {
                    var menuExpanded by remember { mutableStateOf(false) }
                    AppMenu(
                        expanded = menuExpanded,
                        onDismiss = { menuExpanded = false },
                        items = listOf(
                            MenuItem(
                                label = stringResource(R.string.trip_detail_delete_confirm),
                                destructive = true,
                            ) {
                                menuExpanded = false
                                onEvent(TripDetailEvent.DeleteClicked)
                            },
                        ),
                        anchor = {
                            AppIconButton(
                                icon = Icons.Filled.MoreVert,
                                contentDescription = stringResource(R.string.trip_detail_menu),
                                onClick = { menuExpanded = true },
                                modifier = Modifier.testTag("tripDetail_menuButton"),
                            )
                        },
                    )
                },
            )
        },
        contentPadding = false,
    ) {
        if (trip == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppTheme.spacing.inset.xl)
                    .testTag("tripDetail_skeleton"),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.sm),
            ) {
                repeat(SKELETON_ROWS) { AppSkeleton() }
            }
        } else {
            val formats = LocalFormats.current
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppTheme.spacing.inset.xl),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
            ) {
                TripStatusDot(status = trip.status(state.today))

                AppTabs(
                    tabs = listOf(
                        TabItem(stringResource(R.string.trip_detail_tab_itinerary)),
                        TabItem(stringResource(R.string.trip_detail_tab_notes)),
                        TabItem(stringResource(R.string.trip_detail_tab_budget)),
                    ),
                    selectedIndex = state.selectedTab,
                    onSelect = { onEvent(TripDetailEvent.TabSelected(it)) },
                    modifier = Modifier.testTag("tripDetail_tabs"),
                )

                when (state.selectedTab) {
                    TripDetailState.TAB_NOTES -> AppText(
                        text = trip.notes.ifBlank { stringResource(R.string.trip_detail_no_notes) },
                        modifier = Modifier.testTag("tripDetail_notesValue"),
                    )

                    TripDetailState.TAB_BUDGET -> AppText(
                        text = "${formats.money(trip.budgetMinMinor)} – ${formats.money(trip.budgetMaxMinor)}",
                        role = TextRole.Numeric,
                        modifier = Modifier.testTag("tripDetail_budgetValue"),
                    )

                    else -> AppFieldGroup(
                        label = stringResource(R.string.trip_detail_itinerary_label),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tripDetail_itineraryGroup"),
                    ) {
                        AppFieldGroupRow {
                            AppDescriptionList(
                                rows = listOf(
                                    DescriptionRow(
                                        stringResource(R.string.trip_detail_destination_label),
                                        trip.destinationName,
                                    ),
                                    DescriptionRow(
                                        stringResource(R.string.trip_detail_dates_label),
                                        "${formats.date(trip.startDate)} – ${formats.date(trip.endDate)}",
                                    ),
                                ),
                            )
                        }
                        AppFieldGroupRow(last = true) {
                            AppDescriptionList(
                                rows = listOf(
                                    DescriptionRow(
                                        stringResource(R.string.trip_detail_travelers_label),
                                        trip.travelers.toString(),
                                        numeric = true,
                                    ),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

private const val SKELETON_ROWS = 4

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    TripDetailScreen(state = TripDetailState.PREVIEW) {}
}

@ScreenPreview
@Composable
private fun BudgetTabPreview() = ThemedScreenPreview {
    TripDetailScreen(
        state = TripDetailState.PREVIEW.copy(selectedTab = TripDetailState.TAB_BUDGET),
    ) {}
}

@ScreenPreview
@Composable
private fun LoadingPreview() = ThemedScreenPreview {
    TripDetailScreen(state = TripDetailState.PREVIEW.copy(trip = null)) {}
}
