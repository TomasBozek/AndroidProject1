package com.example.androidproject1.feature.template.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object TemplateDestination : NavKey

fun EntryProviderScope<NavKey>.templateDestination(backStack: NavBackStack<NavKey>) {
    entry<TemplateDestination> {
        val viewModel: TemplateViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            // Turn each TemplateNavigation case into a backStack call here — `backStack.add(Key)`
            // to push, `backStack.removeLastOrNull()` to pop. To reach another feature, take a
            // lambda parameter instead and wire it in AppNavHost.
            onNavigation = { navigation ->
            },
        ) { state, onEvent ->
            TemplateScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
