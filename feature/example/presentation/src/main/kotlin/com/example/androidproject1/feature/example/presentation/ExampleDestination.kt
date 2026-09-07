package com.example.androidproject1.feature.example.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.androidproject1.core.ui.component.Screen
import com.example.androidproject1.core.ui.util.CommandEffect
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object ExampleDestination

fun NavGraphBuilder.exampleDestination(navController: NavHostController) {
    composable<ExampleDestination> {
        val viewModel: ExampleViewModel = koinViewModel()

        Screen(viewModel = viewModel) { state, onEvent ->
            ExampleScreen(
                state = state,
                onEvent = onEvent,
            )
        }

        CommandEffect(commandFlow = viewModel.direction) { direction ->
        }
    }
}
