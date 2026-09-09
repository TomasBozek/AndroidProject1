package com.example.androidproject1.feature.catalog.presentation.productdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppIconButton
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.catalog.presentation.R
import com.example.androidproject1.feature.catalog.presentation.asPrice

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppText(text = product.name, role = TextRole.Display)
                // The icon carries the same string as its contentDescription, so the screen
                // reader and the test reach it the same way — and the label says what the tap
                // will do, not what the state is.
                val favouriteLabel = stringResource(
                    if (state.isFavourite) {
                        R.string.product_detail_favourite_remove
                    } else {
                        R.string.product_detail_favourite_add
                    },
                )
                AppIconButton(
                    icon = if (state.isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = favouriteLabel,
                    onClick = { onEvent(ProductDetailEvent.FavouriteToggled) },
                    modifier = Modifier.testTag("productDetail_favouriteButton"),
                )
            }
            AppText(text = product.price.asPrice(), role = TextRole.DisplayLarge)
            AppText(text = product.description, role = TextRole.BodyLarge)
            AppButton(
                label = stringResource(R.string.product_detail_add_to_cart),
                onClick = { onEvent(ProductDetailEvent.AddToCartClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("productDetail_addToCartButton"),
            )
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

/** The filled heart is a different render, so it is a golden of its own. */
@ScreenPreview
@Composable
private fun FavouritePreview() = ThemedScreenPreview {
    ProductDetailScreen(
        state = ProductDetailState.PREVIEW.copy(isFavourite = true),
    ) {}
}
