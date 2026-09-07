package com.example.androidproject1.feature.home.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.androidproject1.core.ui.component.Screen
import com.example.androidproject1.core.ui.util.CommandEffect
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object HomeDestination

/**
 * @param navigateToSettings supplied by the app module. Passing cross-feature navigation in as a
 * lambda is what keeps this module from depending on `:feature:settings:presentation`.
 */
fun NavGraphBuilder.homeDestination(
    navController: NavHostController,
    navigateToSettings: () -> Unit,
) {
    composable<HomeDestination> {
        val viewModel: HomeViewModel = koinViewModel()

        Screen(viewModel = viewModel) { state, onEvent ->
            HomeScreen(
                state = state,
                onEvent = onEvent,
            )
        }

        CommandEffect(commandFlow = viewModel.direction) { direction ->
            when (direction) {
                HomeDirection.Settings -> navigateToSettings()
            }
        }
    }
}
