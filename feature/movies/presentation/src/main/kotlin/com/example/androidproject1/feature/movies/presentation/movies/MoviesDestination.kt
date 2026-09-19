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
            onNavigation = { navigation ->
                when (navigation) {
                    // The detail arrives with G1U3; until then a row is a tap that goes nowhere.
                    is MoviesNavigation.MovieDetail -> Unit
                    MoviesNavigation.NavigateUp -> backStack.removeLastOrNull()
                }
            },
        ) { state, onEvent ->
            MoviesScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
