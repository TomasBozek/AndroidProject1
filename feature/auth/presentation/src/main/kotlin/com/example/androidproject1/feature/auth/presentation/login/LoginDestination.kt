package com.example.androidproject1.feature.auth.presentation.login

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.core.ui.component.Screen
import com.example.androidproject1.feature.auth.presentation.signup.SignUpDestination
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object LoginDestination : NavKey

fun EntryProviderScope<NavKey>.loginDestination(backStack: NavBackStack<NavKey>) {
    entry<LoginDestination> {
        val viewModel: LoginViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    LoginNavigation.SignUp -> backStack.add(SignUpDestination)
                }
            },
        ) { state, onEvent ->
            LoginScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
