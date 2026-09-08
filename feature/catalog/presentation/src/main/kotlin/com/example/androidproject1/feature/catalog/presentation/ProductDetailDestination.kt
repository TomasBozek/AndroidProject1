package com.example.androidproject1.feature.catalog.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.androidproject1.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data class ProductDetailDestination(val productId: String)

fun NavGraphBuilder.productDetailDestination(navController: NavHostController) {
    composable<ProductDetailDestination> {
        // The route's arguments reach the ViewModel through its SavedStateHandle — see
        // ProductDetailViewModel.args. Nothing to pass in here.
        val viewModel: ProductDetailViewModel = koinViewModel()

        Screen(viewModel = viewModel) { state, onEvent ->
            ProductDetailScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
