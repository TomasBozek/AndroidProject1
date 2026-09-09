package com.example.androidproject1.feature.catalog.presentation.productdetail

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.core.ui.component.Screen
import com.example.androidproject1.core.ui.layout.detailPane
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data class ProductDetailDestination(val productId: String) : NavKey

/**
 * @param onAddToCart wired in `AppNavHost`. The catalog does not depend on the cart, so it names
 * what it wants done and the nav host does it.
 */
fun EntryProviderScope<NavKey>.productDetailDestination(
    backStack: NavBackStack<NavKey>,
    onAddToCart: (String) -> Unit,
) {
    // The detail half of the pair; see `productsDestination` for what the metadata buys.
    entry<ProductDetailDestination>(metadata = detailPane()) { key ->
        // The route key is passed straight into the ViewModel, so it is available in `init` and
        // comes back with the entry after process death.
        val viewModel: ProductDetailViewModel = koinViewModel { parametersOf(key) }

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    is ProductDetailNavigation.AddToCart -> onAddToCart(navigation.productId)
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
