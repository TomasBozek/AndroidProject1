package com.example.androidproject1.feature.template.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.androidproject1.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

/**
 * A route that carries arguments. `create_screen.py --with-args` clones this set rather than the
 * plain one; the difference is entirely in this file, the ViewModel and the test.
 */
@Serializable
data class TemplateArgsDestination(val templateId: String)

fun NavGraphBuilder.templateArgsDestination(navController: NavHostController) {
    composable<TemplateArgsDestination> {
        // No argument is passed in here. The route reaches the ViewModel through its
        // SavedStateHandle, which means it is available in `init` and survives process death —
        // neither of which is true of a LaunchedEffect that calls a load() function.
        val viewModel: TemplateArgsViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
            },
        ) { state, onEvent ->
            TemplateArgsScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
