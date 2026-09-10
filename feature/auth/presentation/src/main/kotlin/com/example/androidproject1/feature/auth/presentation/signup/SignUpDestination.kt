package com.example.androidproject1.feature.auth.presentation.signup

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.service.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object SignUpDestination : NavKey

fun EntryProviderScope<NavKey>.signUpDestination(backStack: NavBackStack<NavKey>) {
    entry<SignUpDestination> {
        val viewModel: SignUpViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    SignUpNavigation.Login -> backStack.removeLastOrNull()
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
