package com.example.androidproject1.feature.launch.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.androidproject1.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object LaunchDestination

/** No navController needed: this screen never navigates itself — see LaunchNavigation. */
fun NavGraphBuilder.launchDestination() {
    composable<LaunchDestination> {
        val viewModel: LaunchViewModel = koinViewModel()

        Screen(viewModel = viewModel) { state, onEvent ->
            LaunchScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
