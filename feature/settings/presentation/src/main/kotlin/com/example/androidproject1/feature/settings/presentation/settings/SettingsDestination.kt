package com.example.androidproject1.feature.settings.presentation.settings

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.core.ui.component.Screen
import com.example.androidproject1.feature.settings.presentation.permissions.SettingsPermissionsDestination
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object SettingsDestination : NavKey

/**
 * @param navigateToProfile where the profile screen lives.
 * @param navigateToComponents where the gallery lives. A presentation module never depends on
 *   another feature's presentation, so both jumps arrive as lambdas and are wired in `AppNavHost`.
 */
fun EntryProviderScope<NavKey>.settingsDestination(
    backStack: NavBackStack<NavKey>,
    navigateToProfile: () -> Unit,
    navigateToComponents: () -> Unit,
) {
    entry<SettingsDestination> {
        val viewModel: SettingsViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    SettingsNavigation.Profile -> navigateToProfile()
                    SettingsNavigation.Permissions -> backStack.add(SettingsPermissionsDestination)
                    SettingsNavigation.Components -> navigateToComponents()
                }
            },
        ) { state, onEvent ->
            SettingsScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
