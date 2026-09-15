package com.example.androidproject1.feature.devmenu.presentation.devmenu

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.feature.devmenu.presentation.DevMenuJump
import com.example.androidproject1.service.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data object DevMenuDestination : NavKey

/**
 * @param jumps the screens the menu opens directly. Other features' presentations, so each
 *   arrives as a lambda inside a [DevMenuJump], built in `AppNavHost` — inside the branch that a
 *   `prod` build folds away, which is what lets R8 drop this feature and everything the list names.
 */
fun EntryProviderScope<NavKey>.devMenuDestination(
    backStack: NavBackStack<NavKey>,
    jumps: List<DevMenuJump>,
) {
    entry<DevMenuDestination> {
        // In through Koin the way a route key goes, so the state carries the list from `init`.
        val viewModel: DevMenuViewModel = koinViewModel { parametersOf(jumps) }

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    DevMenuNavigation.NavigateUp -> backStack.removeLastOrNull()
                    is DevMenuNavigation.Jump -> navigation.jump.navigate()
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
