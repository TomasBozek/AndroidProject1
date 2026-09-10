package com.example.androidproject1.feature.gallery.presentation.gallery

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.core.ui.component.Screen
import com.example.androidproject1.feature.gallery.presentation.gallerydetail.GalleryDetailDestination
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object GalleryDestination : NavKey

fun EntryProviderScope<NavKey>.galleryDestination(backStack: NavBackStack<NavKey>) {
    entry<GalleryDestination> {
        val viewModel: GalleryViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            // Turn each GalleryNavigation case into a backStack call here — `backStack.add(Key)`
            // to push, `backStack.removeLastOrNull()` to pop. To reach another feature, take a
            // lambda parameter instead and wire it in AppNavHost.
            onNavigation = { navigation ->
                when (navigation) {
                    is GalleryNavigation.ToComponent ->
                        backStack.add(GalleryDetailDestination(navigation.id))

                    GalleryNavigation.NavigateUp -> backStack.removeLastOrNull()
                }
            },
        ) { state, onEvent ->
            GalleryScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
