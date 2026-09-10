package com.example.androidproject1.feature.catalog.presentation.products

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.core.ui.component.Screen
import com.example.androidproject1.core.ui.layout.listPane
import com.example.androidproject1.feature.catalog.presentation.component.ProductDetailPlaceholder
import com.example.androidproject1.feature.catalog.presentation.productdetail.ProductDetailDestination
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data class ProductsDestination(val categoryId: String, val categoryName: String) : NavKey

/**
 * The list half of the catalog's list–detail pair.
 *
 * The metadata is all there is to it: on a wide screen `ListDetailSceneStrategy` puts this pane and
 * `ProductDetailDestination` side by side, and on a phone the same back stack plays out one screen
 * at a time with nothing here behaving differently. The placeholder is what the detail pane shows
 * before anything is picked — a second pane that is blank looks broken.
 */
fun EntryProviderScope<NavKey>.productsDestination(backStack: NavBackStack<NavKey>) {
    entry<ProductsDestination>(
        metadata = listPane(detailPlaceholder = { ProductDetailPlaceholder() }),
    ) { key ->
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

                    ProductsNavigation.NavigateUp -> backStack.removeLastOrNull()
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
