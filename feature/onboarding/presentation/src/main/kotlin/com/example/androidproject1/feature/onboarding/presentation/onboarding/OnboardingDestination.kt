package com.example.androidproject1.feature.onboarding.presentation.onboarding

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object OnboardingDestination : NavKey

/**
 * The root of the third flow, and the only screen in it.
 *
 * [backStack] is taken for the shape every destination has, and deliberately not used: there is
 * nowhere to go from the tour. Finishing writes the stored flag and `MainViewModel` replaces the
 * stack with the auth flow — a screen never switches flows itself.
 */
fun EntryProviderScope<NavKey>.onboardingDestination(
    @Suppress("unused") backStack: NavBackStack<NavKey>,
) {
    entry<OnboardingDestination> {
        val viewModel: OnboardingViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { },
        ) { state, onEvent ->
            OnboardingScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
