package com.example.androidproject1.feature.home.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.androidproject1.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object HomeDestination

/**
 * @param navigateToSettings wired in AppNavHost, so this module needs no dependency on settings.
 * @param navigateToCatalog wired in AppNavHost, so this module needs no dependency on catalog.
 */
fun NavGraphBuilder.homeDestination(
    navController: NavHostController,
    navigateToSettings: () -> Unit,
    navigateToCatalog: () -> Unit,
) {
    composable<HomeDestination> {
        val viewModel: HomeViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    HomeNavigation.Settings -> navigateToSettings()
                    HomeNavigation.Catalog -> navigateToCatalog()
                }
            },
        ) { state, onEvent ->
            HomeScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
