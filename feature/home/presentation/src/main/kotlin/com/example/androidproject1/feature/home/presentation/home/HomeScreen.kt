package com.example.androidproject1.feature.home.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppCard
import com.example.androidproject1.core.ui.component.AppEmptyState
import com.example.androidproject1.core.ui.component.AppIconButton
import com.example.androidproject1.core.ui.component.AppListItem
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppSectionHeader
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.ButtonKind
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.home.presentation.R
import com.example.androidproject1.service.core.ui.text.resolve

@Composable
fun HomeScreen(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
) {
    AppScaffold(screenId = "HomeScreen") {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
        ) {
            AppText(text = state.greeting.resolve(), role = TextRole.Display)

            // The ways into Inventory and Movies: neither has a tab of its own (D59), so a card is
            // the door — side by side, so the favourites below stay on the first screen.
            Row(
                // The taller card sets the height, so the two read as a pair.
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.md),
            ) {
                AppCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("home_inventoryCard"),
                ) {
                    AppText(text = stringResource(R.string.home_inventory_title), role = TextRole.Title)
                    AppText(
                        text = pluralStringResource(
                            R.plurals.home_inventory_count,
                            state.inventoryCount,
                            state.inventoryCount,
                        ),
                        role = TextRole.Secondary,
                    )
                    AppButton(
                        label = stringResource(R.string.home_inventory_open),
                        onClick = { onEvent(HomeEvent.InventoryClicked) },
                        kind = ButtonKind.Outline,
                        modifier = Modifier
                            .padding(top = AppTheme.spacing.stack.sm)
                            .testTag("home_inventoryButton"),
                    )
                }
                AppCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("home_moviesCard"),
                ) {
                    AppText(text = stringResource(R.string.home_movies_title), role = TextRole.Title)
                    AppText(text = stringResource(R.string.home_movies_subtitle), role = TextRole.Secondary)
                    AppButton(
                        label = stringResource(R.string.home_movies_open),
                        onClick = { onEvent(HomeEvent.MoviesClicked) },
                        kind = ButtonKind.Outline,
                        modifier = Modifier
                            .padding(top = AppTheme.spacing.stack.sm)
                            .testTag("home_moviesButton"),
                    )
                }
            }

            AppSectionHeader(title = stringResource(R.string.home_favourites_title))

            if (state.favourites.isEmpty()) {
                // Not a ContentState: an empty favourites list is a section of this screen, not
                // the screen failing to load. Screen() would replace the greeting too.
                AppEmptyState(
                    title = stringResource(R.string.home_favourites_empty_title),
                    message = stringResource(R.string.home_favourites_empty),
                    modifier = Modifier.testTag("home_favouritesEmpty"),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.testTag("home_favouritesList"),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.xs),
                ) {
                    items(items = state.favourites, key = { it.id }) { product ->
                        AppListItem(
                            headline = product.name,
                            supporting = product.description,
                            modifier = Modifier.testTag("home_favouriteItem"),
                            trailing = {
                                AppIconButton(
                                    icon = Icons.Filled.Close,
                                    contentDescription = stringResource(
                                        R.string.home_favourite_remove,
                                        product.name,
                                    ),
                                    onClick = {
                                        onEvent(HomeEvent.FavouriteRemoved(product.id))
                                    },
                                    modifier = Modifier.testTag("home_favouriteRemoveButton"),
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    HomeScreen(
        state = HomeState.PREVIEW,
    ) {}
}

/** Where the layout is most likely to be wrong, so it is a golden of its own. */
@ScreenPreview
@Composable
private fun EmptyPreview() = ThemedScreenPreview {
    HomeScreen(
        state = HomeState.PREVIEW.copy(favourites = emptyList()),
    ) {}
}
