package com.example.androidproject1.feature.settings.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.androidproject1.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object SettingsDestination

fun NavGraphBuilder.settingsDestination(navController: NavHostController) {
    composable<SettingsDestination> {
        val viewModel: SettingsViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    SettingsNavigation.NavigateUp -> navController.navigateUp()
                }
            },
        ) { state, onEvent ->
            SettingsScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
