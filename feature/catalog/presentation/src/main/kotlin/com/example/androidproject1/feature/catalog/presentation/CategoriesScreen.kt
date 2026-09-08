package com.example.androidproject1.feature.catalog.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview

@Composable
fun CategoriesScreen(
    state: CategoriesState,
    onEvent: (CategoriesEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Edge to edge: a screen without a Scaffold pads itself.
            .safeDrawingPadding(),
    ) {
        Text(
            text = stringResource(R.string.categories_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(24.dp),
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.categories, key = { it.id }) { category ->
                ListItem(
                    headlineContent = { Text(text = category.name) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEvent(CategoriesEvent.CategoryClicked(category)) },
                )
                HorizontalDivider()
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
