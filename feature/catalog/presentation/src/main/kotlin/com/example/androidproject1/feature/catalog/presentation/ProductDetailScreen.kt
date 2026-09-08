package com.example.androidproject1.feature.catalog.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.theme.AppTheme

@Composable
fun ProductDetailScreen(
    state: ProductDetailState,
    onEvent: (ProductDetailEvent) -> Unit,
) {
    AppScaffold(screenId = "ProductDetailScreen") {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.sm),
        ) {
            val product = state.product
            AppText(text = product.name, role = TextRole.Display)
            AppText(text = product.price.asPrice(), role = TextRole.DisplayLarge)
            AppText(text = product.description, role = TextRole.BodyLarge)
        }
    }
}

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    ProductDetailScreen(
        state = ProductDetailState.PREVIEW,
    ) {}
}
