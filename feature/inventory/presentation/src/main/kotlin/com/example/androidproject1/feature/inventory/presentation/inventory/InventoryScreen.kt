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
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppAvatar
import com.example.androidproject1.core.ui.component.AppEmptyState
import com.example.androidproject1.core.ui.component.AppFab
import com.example.androidproject1.core.ui.component.AppListItem
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppScrollShadow
import com.example.androidproject1.core.ui.component.AppSearchField
import com.example.androidproject1.core.ui.component.AppSkeleton
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.component.ScrollEdge
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.inventory.presentation.R
import com.example.androidproject1.feature.inventory.presentation.component.ItemConditionTag
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
        topBar = { AppTopBar(title = stringResource(R.string.inventory_title)) },
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
                        message = if (state.query.isBlank()) {
                            stringResource(R.string.inventory_empty_message)
                        } else {
                            stringResource(R.string.inventory_empty_filtered_message, state.query)
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
                                    AppListItem(
                                        headline = item.name,
                                        supporting = "${item.owner} · ${formats.money(item.priceMinor)}",
                                        onClick = { onEvent(InventoryEvent.ItemClicked(item)) },
                                        leading = { AppAvatar(name = item.owner) },
                                        trailing = { ItemConditionTag(condition = item.condition) },
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
