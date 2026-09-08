package com.example.androidproject1.feature.home.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object HomeDestination : NavKey

/**
 * @param navigateToSettings wired in AppNavHost, so this module needs no dependency on settings.
 * @param navigateToCatalog wired in AppNavHost, so this module needs no dependency on catalog.
 */
fun EntryProviderScope<NavKey>.homeDestination(
    backStack: NavBackStack<NavKey>,
    navigateToSettings: () -> Unit,
    navigateToCatalog: () -> Unit,
) {
    entry<HomeDestination> {
        val viewModel: HomeViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    HomeNavigation.Settings -> navigateToSettings()
                    HomeNavigation.Catalog -> navigateToCatalog()
                }
            },
        ) { state, onEvent ->
            HomeScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
