package com.example.androidproject1.feature.cart.presentation.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppBottomActionBar
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppEmptyState
import com.example.androidproject1.core.ui.component.AppIconButton
import com.example.androidproject1.core.ui.component.AppListItem
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppStepper
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.component.ButtonKind
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.cart.presentation.R
import com.example.androidproject1.service.core.ui.format.LocalFormats
import com.example.androidproject1.service.core.ui.text.resolve

@Composable
fun CartScreen(
    state: CartState,
    onEvent: (CartEvent) -> Unit,
) {
    AppScaffold(
        screenId = "CartScreen",
        topBar = { AppTopBar(title = stringResource(R.string.cart_title)) },
    ) {
        val formats = LocalFormats.current
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.sm),
        ) {
            if (state.isEmpty) {
                AppEmptyState(
                    title = stringResource(R.string.cart_empty_title),
                    message = stringResource(R.string.cart_empty_message),
                    actionLabel = stringResource(R.string.cart_add_item),
                    onAction = { onEvent(CartEvent.AddItemClicked) },
                    modifier = Modifier.testTag("cart_emptyState"),
                )
                return@Column
            }

            AppText(text = state.itemCountLabel.resolve(), role = TextRole.Title)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag("cart_itemList"),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.xs),
            ) {
                items(items = state.items, key = { it.productId }) { item ->
                    AppListItem(
                        headline = item.name,
                        supporting = formats.money(item.lineTotal),
                        modifier = Modifier.testTag("cart_item"),
                        trailing = {
                            AppIconButton(
                                icon = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.cart_remove, item.name),
                                onClick = { onEvent(CartEvent.ItemRemoved(item.productId)) },
                                modifier = Modifier.testTag("cart_removeButton"),
                            )
                        },
                    )
                    AppStepper(
                        value = item.quantity,
                        onValueChange = { onEvent(CartEvent.QuantityChanged(item.productId, it)) },
                        min = 0,
                        modifier = Modifier.testTag("cart_quantityStepper"),
                    )
                }
            }

            AppButton(
                label = stringResource(R.string.cart_add_item),
                onClick = { onEvent(CartEvent.AddItemClicked) },
                kind = ButtonKind.Ghost,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cart_addItemButton"),
            )

            AppBottomActionBar(
                actionLabel = stringResource(R.string.cart_checkout),
                onAction = { onEvent(CartEvent.CheckoutClicked) },
                label = stringResource(R.string.cart_total),
                value = formats.money(state.total),
                modifier = Modifier.testTag("cart_checkoutButton"),
            )
        }
    }
}

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    CartScreen(state = CartState.PREVIEW) {}
}

/** The empty cart is where the layout collapses, so it is a golden of its own. */
@ScreenPreview
@Composable
private fun EmptyPreview() = ThemedScreenPreview {
    CartScreen(state = CartState.EMPTY) {}
}
