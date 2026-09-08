package com.example.androidproject1.feature.settings.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object SettingsDestination : NavKey

fun EntryProviderScope<NavKey>.settingsDestination(backStack: NavBackStack<NavKey>) {
    entry<SettingsDestination> {
        val viewModel: SettingsViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    SettingsNavigation.NavigateUp -> backStack.removeLastOrNull()
                }
            },
        ) { state, onEvent ->
            SettingsScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
