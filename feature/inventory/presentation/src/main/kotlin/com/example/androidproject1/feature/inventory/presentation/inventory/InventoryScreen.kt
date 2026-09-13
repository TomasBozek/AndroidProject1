package com.example.androidproject1.feature.inventory.presentation.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppAvatar
import com.example.androidproject1.core.ui.component.AppBadge
import com.example.androidproject1.core.ui.component.AppCheckbox
import com.example.androidproject1.core.ui.component.AppEmptyState
import com.example.androidproject1.core.ui.component.AppFab
import com.example.androidproject1.core.ui.component.AppIconButton
import com.example.androidproject1.core.ui.component.AppListItem
import com.example.androidproject1.core.ui.component.AppMenu
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppScrollShadow
import com.example.androidproject1.core.ui.component.AppSearchField
import com.example.androidproject1.core.ui.component.AppSkeleton
import com.example.androidproject1.core.ui.component.AppSpinner
import com.example.androidproject1.core.ui.component.AppToolbar
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.component.CheckState
import com.example.androidproject1.core.ui.component.IconButtonKind
import com.example.androidproject1.core.ui.component.MenuItem
import com.example.androidproject1.core.ui.component.ScrollEdge
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.inventory.domain.ItemTag
import com.example.androidproject1.feature.inventory.presentation.R
import com.example.androidproject1.feature.inventory.presentation.component.ItemConditionTag
import com.example.androidproject1.feature.inventory.presentation.component.ItemFilterSheet
import com.example.androidproject1.service.core.ui.format.LocalFormats

@Composable
fun InventoryScreen(
    state: InventoryState,
    onEvent: (InventoryEvent) -> Unit,
) {
    AppScaffold(
        // A tab root in E3S6; until then it is reached from Home, and the bottom bar is what
        // leaves it either way, so there is no up arrow.
        screenId = "InventoryScreen",
        topBar = {
            if (state.selecting) {
                // Selection mode replaces the bar: what is selected, and what can be done to it.
                AppToolbar(
                    title = stringResource(R.string.inventory_selected_count, state.selectedIds.size),
                    modifier = Modifier.testTag("inventory_selectionGroup"),
                ) {
                    AppCheckbox(
                        checked = state.selectAllState,
                        onCheckedChange = { onEvent(InventoryEvent.SelectAllChanged(it)) },
                        label = stringResource(R.string.inventory_select_all),
                        inverse = true,
                        modifier = Modifier.testTag("inventory_selectAllCheckbox"),
                    )
                    AppIconButton(
                        icon = Icons.Filled.Favorite,
                        contentDescription = stringResource(R.string.inventory_favourite_selected),
                        onClick = { onEvent(InventoryEvent.FavouriteSelectedClicked) },
                        kind = IconButtonKind.Inverse,
                        modifier = Modifier.testTag("inventory_favouriteButton"),
                    )
                    AppIconButton(
                        icon = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.inventory_delete_selected),
                        onClick = { onEvent(InventoryEvent.DeleteSelectedClicked) },
                        kind = IconButtonKind.Inverse,
                        modifier = Modifier.testTag("inventory_deleteButton"),
                    )
                    AppIconButton(
                        icon = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.inventory_selection_clear),
                        onClick = { onEvent(InventoryEvent.SelectionCleared) },
                        kind = IconButtonKind.Inverse,
                        modifier = Modifier.testTag("inventory_selectionClearButton"),
                    )
                }
            } else {
                AppTopBar(
                    title = stringResource(R.string.inventory_title),
                    actions = {
                        Box {
                            AppIconButton(
                                icon = Icons.Filled.Search,
                                contentDescription = stringResource(R.string.inventory_filter_title),
                                onClick = { onEvent(InventoryEvent.FilterClicked) },
                                modifier = Modifier.testTag("inventory_filterButton"),
                            )
                            AppBadge(
                                count = state.filter.activeCount,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .testTag("inventory_filterBadge"),
                            )
                        }
                        var sortExpanded by remember { mutableStateOf(false) }
                        AppMenu(
                            expanded = sortExpanded,
                            onDismiss = { sortExpanded = false },
                            items = ItemSort.entries.map { sort ->
                                MenuItem(label = stringResource(sort.labelRes()), testTag = "inventory_sortItem") {
                                    sortExpanded = false
                                    onEvent(InventoryEvent.SortSelected(sort))
                                }
                            },
                            anchor = {
                                AppIconButton(
                                    icon = Icons.AutoMirrored.Filled.List,
                                    contentDescription = stringResource(R.string.inventory_sort_title),
                                    onClick = { sortExpanded = true },
                                    modifier = Modifier.testTag("inventory_sortButton"),
                                )
                            },
                        )
                    },
                )
            }
        },
        contentPadding = false,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                AppSearchField(
                    value = state.query,
                    onValueChange = { onEvent(InventoryEvent.QueryChanged(it)) },
                    placeholder = stringResource(R.string.inventory_search_placeholder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.spacing.inset.lg, vertical = AppTheme.spacing.inset.md)
                        .testTag("inventory_searchField"),
                )
                when {
                    state.loading -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = AppTheme.spacing.inset.lg)
                            .testTag("inventory_skeleton"),
                        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.sm),
                    ) {
                        repeat(SKELETON_ROWS) { AppSkeleton(height = AppTheme.density.listRowHeight) }
                    }

                    state.items.isEmpty() -> AppEmptyState(
                        title = stringResource(R.string.inventory_empty_title),
                        message = when {
                            state.query.isNotBlank() -> stringResource(
                                R.string.inventory_empty_filtered_message,
                                state.query,
                            )
                            !state.filter.isEmpty -> stringResource(R.string.inventory_empty_filter_message)
                            else -> stringResource(R.string.inventory_empty_message)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(AppTheme.spacing.inset.xl)
                            .testTag("inventory_empty"),
                    )

                    else -> {
                        val formats = LocalFormats.current
                        val scroll = rememberLazyListState()
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                state = scroll,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("inventory_list"),
                            ) {
                                items(items = state.items, key = { it.id }) { item ->
                                    val pending = item.id in state.pendingIds
                                    AppListItem(
                                        headline = item.name,
                                        supporting = "${item.owner} · ${formats.money(item.priceMinor)}",
                                        onClick = { onEvent(InventoryEvent.ItemClicked(item)) },
                                        onLongClick = { onEvent(InventoryEvent.ItemLongPressed(item)) },
                                        leading = {
                                            if (state.selecting) {
                                                AppCheckbox(
                                                    checked = if (item.id in
                                                        state.selectedIds
                                                    ) {
                                                        CheckState.On
                                                    } else {
                                                        CheckState.Off
                                                    },
                                                    onCheckedChange = { onEvent(InventoryEvent.ItemChecked(item, it)) },
                                                    // The row already says the name; the box says the state.
                                                    label = "",
                                                    modifier = Modifier.testTag("inventory_itemCheckbox"),
                                                )
                                            } else {
                                                AppAvatar(name = item.owner)
                                            }
                                        },
                                        trailing = {
                                            if (pending) {
                                                AppSpinner(modifier = Modifier.testTag("inventory_itemProgress"))
                                            } else {
                                                ItemConditionTag(condition = item.condition)
                                            }
                                        },
                                        modifier = Modifier.testTag("inventory_item"),
                                    )
                                }
                            }
                            // The cue that the list carries on past the fold — only while there is more.
                            AppScrollShadow(scroll, ScrollEdge.Top, Modifier.align(Alignment.TopCenter))
                            AppScrollShadow(scroll, ScrollEdge.Bottom, Modifier.align(Alignment.BottomCenter))
                        }
                    }
                }
            }

            if (!state.selecting) {
                AppFab(
                    icon = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.inventory_new_item),
                    onClick = { onEvent(InventoryEvent.NewItemClicked) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(AppTheme.spacing.inset.lg)
                        .testTag("inventory_newButton"),
                )
            }
        }

        if (state.filterSheetOpen) {
            ItemFilterSheet(state = state, onEvent = onEvent)
        }
    }
}

private fun ItemSort.labelRes() = when (this) {
    ItemSort.Name -> R.string.inventory_sort_name
    ItemSort.Price -> R.string.inventory_sort_price
    ItemSort.Acquired -> R.string.inventory_sort_acquired
}

private const val SKELETON_ROWS = 6

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    InventoryScreen(state = InventoryState.PREVIEW) {}
}

@ScreenPreview
@Composable
private fun LoadingPreview() = ThemedScreenPreview {
    InventoryScreen(state = InventoryState.PREVIEW.copy(loading = true)) {}
}

@ScreenPreview
@Composable
private fun EmptyPreview() = ThemedScreenPreview {
    InventoryScreen(state = InventoryState.PREVIEW.copy(query = "piano", items = emptyList())) {}
}

@ScreenPreview
@Composable
private fun SelectingPreview() = ThemedScreenPreview {
    InventoryScreen(
        state = InventoryState.PREVIEW.copy(
            selectedIds = setOf("item-drill"),
            pendingIds = setOf("item-kettle"),
            filter = ItemFilter(tags = setOf(ItemTag.Lent)),
        ),
    ) {}
}
