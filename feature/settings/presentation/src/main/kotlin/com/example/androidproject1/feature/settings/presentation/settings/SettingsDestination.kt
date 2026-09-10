package com.example.androidproject1.feature.settings.presentation.settings

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.feature.settings.presentation.permissions.SettingsPermissionsDestination
import com.example.androidproject1.service.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object SettingsDestination : NavKey

/**
 * @param navigateToProfile where the profile screen lives.
 * @param navigateToDebugMenu where the debug menu lives, or `null` in a build that has none. A
 *   presentation module never depends on another feature's presentation, so both jumps arrive as
 *   lambdas and are wired in `AppNavHost` — and the null is how this screen learns that a `prod`
 *   build has no debug menu to offer.
 */
fun EntryProviderScope<NavKey>.settingsDestination(
    backStack: NavBackStack<NavKey>,
    navigateToProfile: () -> Unit,
    navigateToDebugMenu: (() -> Unit)?,
) {
    entry<SettingsDestination> {
        val viewModel: SettingsViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    SettingsNavigation.Profile -> navigateToProfile()
                    SettingsNavigation.Permissions -> backStack.add(SettingsPermissionsDestination)
                    SettingsNavigation.DebugMenu -> navigateToDebugMenu?.invoke()
                }
            },
        ) { state, onEvent ->
            // Reported rather than passed to the screen: the screen renders from its state and
            // nothing else, so the build fact goes in through the same door a user event does.
            val hasDebugMenu = navigateToDebugMenu != null
            LaunchedEffect(hasDebugMenu) {
                onEvent(SettingsEvent.DebugMenuAvailable(hasDebugMenu))
            }
            SettingsScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
