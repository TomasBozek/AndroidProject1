package com.example.androidproject1.feature.catalog.presentation.categories

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.feature.catalog.presentation.products.ProductsDestination
import com.example.androidproject1.feature.catalog.presentation.search.ProductSearchDestination
import com.example.androidproject1.service.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object CategoriesDestination : NavKey

fun EntryProviderScope<NavKey>.categoriesDestination(backStack: NavBackStack<NavKey>) {
    entry<CategoriesDestination> {
        val viewModel: CategoriesViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    is CategoriesNavigation.Products -> backStack.add(
                        ProductsDestination(categoryId = navigation.categoryId, categoryName = navigation.categoryName),
                    )

                    CategoriesNavigation.Search -> backStack.add(ProductSearchDestination)
                }
            },
        ) { state, onEvent ->
            CategoriesScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
