package com.example.androidproject1.feature.home.presentation.home

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object HomeDestination : NavKey

fun EntryProviderScope<NavKey>.homeDestination(backStack: NavBackStack<NavKey>) {
    entry<HomeDestination> {
        val viewModel: HomeViewModel = koinViewModel()

        Screen(viewModel = viewModel) { state, onEvent ->
            HomeScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
