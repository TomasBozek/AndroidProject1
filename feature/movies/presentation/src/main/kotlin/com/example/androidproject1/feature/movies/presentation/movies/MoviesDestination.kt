package com.example.androidproject1.feature.movies.presentation.movies

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.service.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object MoviesDestination : NavKey

fun EntryProviderScope<NavKey>.moviesDestination(backStack: NavBackStack<NavKey>) {
    entry<MoviesDestination> {
        val viewModel: MoviesViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            // Turn each MoviesNavigation case into a backStack call here — `backStack.add(Key)`
            // to push, `backStack.removeLastOrNull()` to pop. To reach another feature, take a
            // lambda parameter instead and wire it in AppNavHost.
            onNavigation = { navigation ->
            },
        ) { state, onEvent ->
            MoviesScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
