package com.example.androidproject1.feature.devmenu.presentation.playground

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.service.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object DevMenuPlaygroundDestination : NavKey

fun EntryProviderScope<NavKey>.devMenuPlaygroundDestination(backStack: NavBackStack<NavKey>) {
    entry<DevMenuPlaygroundDestination> {
        val viewModel: DevMenuPlaygroundViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    DevMenuPlaygroundNavigation.NavigateUp -> backStack.removeLastOrNull()
                }
            },
        ) { state, onEvent ->
            DevMenuPlaygroundScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
