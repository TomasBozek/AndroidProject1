package com.example.androidproject1.feature.catalog.presentation.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppDivider
import com.example.androidproject1.core.ui.component.AppEmptyState
import com.example.androidproject1.core.ui.component.AppListItem
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppSearchField
import com.example.androidproject1.core.ui.component.AppSectionHeader
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.catalog.presentation.R
import com.example.androidproject1.feature.catalog.presentation.asPrice

/**
 * Search over what browsing has already cached, with the previous searches underneath.
 *
 * Two lists, two loads, and each has its own content id — so a failure on one is retried on its
 * own. `Screen()` draws that failure in place of this whole screen, which is what an inline error
 * is: there is nothing behind it worth showing while the list it replaces is unreadable.
 */
@Composable
fun ProductSearchScreen(
    state: ProductSearchState,
    onEvent: (ProductSearchEvent) -> Unit,
) {
    AppScaffold(
        screenId = "ProductSearchScreen",
        topBar = {
            AppTopBar(
                title = stringResource(R.string.product_search_title),
                onNavigateUp = { onEvent(ProductSearchEvent.NavigateUpClicked) },
            )
        },
        contentPadding = false,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppSearchField(
                value = state.query,
                onValueChange = { onEvent(ProductSearchEvent.QueryChanged(it)) },
                placeholder = stringResource(R.string.product_search_placeholder),
                modifier = Modifier
                    .padding(horizontal = AppTheme.spacing.inset.lg)
                    .testTag("productSearch_queryField"),
            )

            when {
                // A query that matched nothing, which is not the same as a field nobody has typed
                // in yet — the second shows the recent searches instead.
                state.searched && state.results.isEmpty() -> AppEmptyState(
                    title = stringResource(R.string.product_search_no_results_title),
                    message = stringResource(R.string.product_search_no_results_message, state.query),
                    modifier = Modifier.testTag("productSearch_resultsEmpty"),
                )

                state.searched -> ResultList(state = state, onEvent = onEvent)

                else -> RecentList(state = state, onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun ResultList(
    state: ProductSearchState,
    onEvent: (ProductSearchEvent) -> Unit,
) {
    LazyColumn(modifier = Modifier.testTag("productSearch_resultList")) {
        items(state.results, key = { it.id }) { product ->
            AppListItem(
                headline = product.name,
                onClick = { onEvent(ProductSearchEvent.ProductClicked(product)) },
                modifier = Modifier.testTag("productSearch_resultItem"),
                // A price is numeric, so it gets tabular figures and lines up down the column.
                trailing = { AppText(text = product.price.asPrice(), role = TextRole.Numeric) },
            )
            AppDivider()
        }
    }
}

@Composable
private fun RecentList(
    state: ProductSearchState,
    onEvent: (ProductSearchEvent) -> Unit,
) {
    if (state.recents.isEmpty()) {
        AppEmptyState(
            title = stringResource(R.string.product_search_recents_empty_title),
            message = stringResource(R.string.product_search_recents_empty_message),
            modifier = Modifier.testTag("productSearch_recentsEmpty"),
        )
        return
    }

    Column {
        AppSectionHeader(
            title = stringResource(R.string.product_search_recents),
            actionLabel = stringResource(R.string.product_search_recents_clear),
            onAction = { onEvent(ProductSearchEvent.ClearRecentsClicked) },
            modifier = Modifier.padding(horizontal = AppTheme.spacing.inset.lg),
        )
        LazyColumn(modifier = Modifier.testTag("productSearch_recentList")) {
            items(state.recents, key = { it }) { recent ->
                AppListItem(
                    headline = recent,
                    onClick = { onEvent(ProductSearchEvent.RecentClicked(recent)) },
                    modifier = Modifier.testTag("productSearch_recentItem"),
                )
                AppDivider()
            }
        }
    }
}

@ScreenPreview
@Composable
private fun Preview(
    @PreviewParameter(ProductSearchStatePreviews::class) state: ProductSearchState,
) = ThemedScreenPreview {
    ProductSearchScreen(
        state = state,
    ) {}
}
