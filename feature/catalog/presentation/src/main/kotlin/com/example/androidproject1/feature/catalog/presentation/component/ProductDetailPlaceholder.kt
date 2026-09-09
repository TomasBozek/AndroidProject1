package com.example.androidproject1.feature.catalog.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.component.AppEmptyState
import com.example.androidproject1.feature.catalog.presentation.R

/** What the detail pane shows on a wide screen before a product has been picked. */
@Composable
fun ProductDetailPlaceholder(modifier: Modifier = Modifier) {
    AppEmptyState(
        modifier = modifier,
        title = stringResource(R.string.product_detail_placeholder_title),
        message = stringResource(R.string.product_detail_placeholder_message),
    )
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    ProductDetailPlaceholder()
}
