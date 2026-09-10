package com.example.androidproject1.feature.catalog.presentation.productdetail

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.core.ui.layout.detailPane
import com.example.androidproject1.service.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data class ProductDetailDestination(val productId: String) : NavKey

fun EntryProviderScope<NavKey>.productDetailDestination(backStack: NavBackStack<NavKey>) {
    // The detail half of the pair; see `productsDestination` for what the metadata buys.
    entry<ProductDetailDestination>(metadata = detailPane()) { key ->
        // The route key is passed straight into the ViewModel, so it is available in `init` and
        // comes back with the entry after process death.
        val viewModel: ProductDetailViewModel = koinViewModel { parametersOf(key) }

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    ProductDetailNavigation.NavigateUp -> backStack.removeLastOrNull()
                }
            },
        ) { state, onEvent ->
            ProductDetailScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
