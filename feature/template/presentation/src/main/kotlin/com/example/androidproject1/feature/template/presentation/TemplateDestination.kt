package com.example.androidproject1.feature.template.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.androidproject1.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object TemplateDestination

fun NavGraphBuilder.templateDestination(navController: NavHostController) {
    composable<TemplateDestination> {
        val viewModel: TemplateViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            // Turn each TemplateNavigation case into a navController call here. To reach another
            // feature, take a lambda parameter instead and wire it in AppNavHost.
            onNavigation = { navigation ->
            },
        ) { state, onEvent ->
            TemplateScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
