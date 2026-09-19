package com.example.androidproject1.feature.settings.presentation.language

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.service.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object SettingsLanguageDestination : NavKey

fun EntryProviderScope<NavKey>.settingsLanguageDestination(backStack: NavBackStack<NavKey>) {
    entry<SettingsLanguageDestination> {
        val viewModel: SettingsLanguageViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    SettingsLanguageNavigation.NavigateUp -> backStack.removeLastOrNull()
                }
            },
        ) { state, onEvent ->
            SettingsLanguageScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
