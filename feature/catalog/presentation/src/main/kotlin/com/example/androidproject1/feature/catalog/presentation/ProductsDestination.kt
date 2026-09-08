package com.example.androidproject1.feature.catalog.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.androidproject1.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data class ProductsDestination(val categoryId: String, val categoryName: String)

fun NavGraphBuilder.productsDestination(navController: NavHostController) {
    composable<ProductsDestination> {
        // The route's arguments reach the ViewModel through its SavedStateHandle — see
        // ProductsViewModel.args. Nothing to pass in here.
        val viewModel: ProductsViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    is ProductsNavigation.ProductDetail -> navController.navigate(
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
