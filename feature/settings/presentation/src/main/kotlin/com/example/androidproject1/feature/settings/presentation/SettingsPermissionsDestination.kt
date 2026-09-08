package com.example.androidproject1.feature.settings.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object SettingsPermissionsDestination : NavKey

fun EntryProviderScope<NavKey>.settingsPermissionsDestination(backStack: NavBackStack<NavKey>) {
    entry<SettingsPermissionsDestination> {
        val viewModel: SettingsPermissionsViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    SettingsPermissionsNavigation.NavigateUp -> backStack.removeLastOrNull()
                }
            },
        ) { state, onEvent ->
            SettingsPermissionsScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
