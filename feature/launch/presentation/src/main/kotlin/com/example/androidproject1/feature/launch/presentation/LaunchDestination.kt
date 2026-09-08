package com.example.androidproject1.feature.launch.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object LaunchDestination : NavKey

/** No back stack needed: this screen never navigates itself — see LaunchNavigation. */
fun EntryProviderScope<NavKey>.launchDestination() {
    entry<LaunchDestination> {
        val viewModel: LaunchViewModel = koinViewModel()

        Screen(viewModel = viewModel) { state, onEvent ->
            LaunchScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
