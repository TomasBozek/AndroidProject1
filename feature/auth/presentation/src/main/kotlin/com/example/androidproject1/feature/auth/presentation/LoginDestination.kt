package com.example.androidproject1.feature.auth.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.androidproject1.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object LoginDestination

fun NavGraphBuilder.loginDestination(navController: NavHostController) {
    composable<LoginDestination> {
        val viewModel: LoginViewModel = koinViewModel()

        Screen(viewModel = viewModel) { state, onEvent ->
            LoginScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
