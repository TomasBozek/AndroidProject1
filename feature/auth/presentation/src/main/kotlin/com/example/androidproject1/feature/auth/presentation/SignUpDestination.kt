package com.example.androidproject1.feature.auth.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.androidproject1.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object SignUpDestination

fun NavGraphBuilder.signUpDestination(navController: NavHostController) {
    composable<SignUpDestination> {
        val viewModel: SignUpViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    SignUpNavigation.Login -> navController.popBackStack()
                }
            },
        ) { state, onEvent ->
            SignUpScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
