package com.example.androidproject1.feature.home.presentation.home

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.service.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object HomeDestination : NavKey

/**
 * @param navigateToInventory another feature's screen, so the host wires it — see AppNavHost.
 * @param navigateToMovies the same, for the movies card.
 */
fun EntryProviderScope<NavKey>.homeDestination(
    backStack: NavBackStack<NavKey>,
    navigateToInventory: () -> Unit,
    navigateToMovies: () -> Unit,
) {
    entry<HomeDestination> {
        val viewModel: HomeViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    HomeNavigation.OpenInventory -> navigateToInventory()
                    HomeNavigation.OpenMovies -> navigateToMovies()
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
