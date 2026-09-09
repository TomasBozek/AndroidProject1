package com.example.androidproject1.feature.devmenu.presentation.devmenu

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object DevMenuDestination : NavKey

/**
 * @param navigateToComponents where the gallery lives. Another feature's presentation, so it
 *   arrives as a lambda and is wired in `AppNavHost` — inside the branch that a `prod` build folds
 *   away, which is what lets R8 drop both features.
 */
fun EntryProviderScope<NavKey>.devMenuDestination(
    backStack: NavBackStack<NavKey>,
    navigateToComponents: () -> Unit,
) {
    entry<DevMenuDestination> {
        val viewModel: DevMenuViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    DevMenuNavigation.NavigateUp -> backStack.removeLastOrNull()
                    DevMenuNavigation.Components -> navigateToComponents()
                }
            },
        ) { state, onEvent ->
            DevMenuScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
