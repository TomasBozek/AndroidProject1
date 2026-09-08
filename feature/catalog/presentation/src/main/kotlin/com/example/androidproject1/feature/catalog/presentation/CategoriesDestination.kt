package com.example.androidproject1.feature.catalog.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.androidproject1.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object CategoriesDestination

fun NavGraphBuilder.categoriesDestination(navController: NavHostController) {
    composable<CategoriesDestination> {
        val viewModel: CategoriesViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    is CategoriesNavigation.Products -> navController.navigate(
                        ProductsDestination(categoryId = navigation.categoryId, categoryName = navigation.categoryName),
                    )
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
