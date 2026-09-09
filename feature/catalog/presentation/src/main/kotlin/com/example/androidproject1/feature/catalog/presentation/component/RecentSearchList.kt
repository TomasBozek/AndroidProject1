package com.example.androidproject1.feature.catalog.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.component.AppDivider
import com.example.androidproject1.core.ui.component.AppEmptyState
import com.example.androidproject1.core.ui.component.AppListItem
import com.example.androidproject1.core.ui.component.AppSectionHeader
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.catalog.presentation.R

/**
 * What was searched for before, which is what a field nobody has typed in yet shows instead of an
 * empty result list — the two look identical otherwise, and only one of them deserves a message.
 */
@Composable
fun RecentSearchList(
    recents: List<String>,
    onRecentClick: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (recents.isEmpty()) {
        AppEmptyState(
            title = stringResource(R.string.product_search_recents_empty_title),
            message = stringResource(R.string.product_search_recents_empty_message),
            modifier = modifier.testTag("productSearch_recentsEmpty"),
        )
        return
    }

    Column(modifier = modifier) {
        AppSectionHeader(
            title = stringResource(R.string.product_search_recents),
            actionLabel = stringResource(R.string.product_search_recents_clear),
            onAction = onClear,
            modifier = Modifier.padding(horizontal = AppTheme.spacing.inset.lg),
        )
        LazyColumn(modifier = Modifier.testTag("productSearch_recentList")) {
            items(recents, key = { it }) { recent ->
                AppListItem(
                    headline = recent,
                    onClick = { onRecentClick(recent) },
                    modifier = Modifier.testTag("productSearch_recentItem"),
                )
                AppDivider()
            }
        }
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    RecentSearchList(
        recents = listOf("coffee", "a rather long previous search"),
        onRecentClick = {},
        onClear = {},
    )
    RecentSearchList(recents = emptyList(), onRecentClick = {}, onClear = {})
}
