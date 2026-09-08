package com.example.androidproject1.feature.gallery.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * A route that carries arguments. `create_screen.py --with-args` clones this set rather than the
 * plain one; the difference is entirely in this file, the ViewModel and the test.
 */
@Serializable
data class GalleryDetailDestination(val componentId: String) : NavKey

fun EntryProviderScope<NavKey>.galleryDetailDestination(backStack: NavBackStack<NavKey>) {
    entry<GalleryDetailDestination> { key ->
        // The route key goes straight into the ViewModel through Koin, so it is available in
        // `init` and comes back with the entry after process death — neither of which is true of
        // a LaunchedEffect that calls a load() function.
        val viewModel: GalleryDetailViewModel = koinViewModel { parametersOf(key) }

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    GalleryDetailNavigation.Up -> backStack.removeLastOrNull()
                }
            },
        ) { state, onEvent ->
            GalleryDetailScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
