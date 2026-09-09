package com.example.androidproject1.feature.catalog.presentation.categories

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppDivider
import com.example.androidproject1.core.ui.component.AppIconButton
import com.example.androidproject1.core.ui.component.AppListItem
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.feature.catalog.presentation.R

@Composable
fun CategoriesScreen(
    state: CategoriesState,
    onEvent: (CategoriesEvent) -> Unit,
) {
    AppScaffold(
        screenId = "CategoriesScreen",
        // No up arrow: this is a tab root, and the bottom bar is what leaves it.
        topBar = {
            AppTopBar(title = stringResource(R.string.categories_title)) {
                AppIconButton(
                    icon = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.categories_search),
                    onClick = { onEvent(CategoriesEvent.SearchClicked) },
                    modifier = Modifier.testTag("categories_searchButton"),
                )
            }
        },
        contentPadding = false,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("categories_list"),
        ) {
            items(state.categories, key = { it.id }) { category ->
                AppListItem(
                    headline = category.name,
                    onClick = { onEvent(CategoriesEvent.CategoryClicked(category)) },
                    modifier = Modifier.testTag("categories_item"),
                )
                AppDivider()
            }
        }
    }
}

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    CategoriesScreen(
        state = CategoriesState.PREVIEW,
    ) {}
}
