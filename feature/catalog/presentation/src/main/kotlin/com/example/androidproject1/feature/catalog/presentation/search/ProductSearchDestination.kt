package com.example.androidproject1.feature.catalog.presentation.search

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.feature.catalog.presentation.productdetail.ProductDetailDestination
import com.example.androidproject1.service.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object ProductSearchDestination : NavKey

fun EntryProviderScope<NavKey>.productSearchDestination(backStack: NavBackStack<NavKey>) {
    entry<ProductSearchDestination> {
        val viewModel: ProductSearchViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    // The same feature, so this is an ordinary push rather than a lambda: only a
                    // jump into another feature's presentation has to be wired in AppNavHost.
                    is ProductSearchNavigation.ProductDetail ->
                        backStack.add(ProductDetailDestination(productId = navigation.productId))

                    ProductSearchNavigation.NavigateUp -> backStack.removeLastOrNull()
                }
            },
        ) { state, onEvent ->
            ProductSearchScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
