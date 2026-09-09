package com.example.androidproject1.feature.profile.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object ProfileDestination : NavKey

fun EntryProviderScope<NavKey>.profileDestination(backStack: NavBackStack<NavKey>) {
    entry<ProfileDestination> {
        val viewModel: ProfileViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    ProfileNavigation.NavigateUp -> backStack.removeLastOrNull()
                }
            },
        ) { state, onEvent ->
            ProfileScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
