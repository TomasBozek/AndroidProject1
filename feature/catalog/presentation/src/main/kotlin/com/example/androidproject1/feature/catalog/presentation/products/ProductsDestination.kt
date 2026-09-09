package com.example.androidproject1.feature.catalog.presentation.products

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.core.ui.component.Screen
import com.example.androidproject1.feature.catalog.presentation.productdetail.ProductDetailDestination
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data class ProductsDestination(val categoryId: String, val categoryName: String) : NavKey

fun EntryProviderScope<NavKey>.productsDestination(backStack: NavBackStack<NavKey>) {
    entry<ProductsDestination> { key ->
        // The route key is passed straight into the ViewModel, so it is available in `init` and
        // comes back with the entry after process death.
        val viewModel: ProductsViewModel = koinViewModel { parametersOf(key) }

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    is ProductsNavigation.ProductDetail -> backStack.add(
                        ProductDetailDestination(productId = navigation.productId),
                    )
                }
            },
        ) { state, onEvent ->
            ProductsScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
