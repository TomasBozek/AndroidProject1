package com.example.androidproject1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.example.androidproject1.core.ui.component.Screen
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.auth.presentation.LoginDestination
import com.example.androidproject1.feature.home.presentation.HomeDestination
import com.example.androidproject1.feature.launch.presentation.LaunchDestination
import org.koin.androidx.viewmodel.ext.android.viewModel

/** The app's only Activity; everything else is a composable destination. */
class MainActivity : ComponentActivity() {

    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // No KoinContext wrapper: since Koin 4.2, startKoin() sets the Compose context up.
            AppTheme {
                MainContent()
            }
        }
    }

    @Composable
    private fun MainContent() {
        // The app always starts on the launch screen; MainViewModel decides when to leave it.
        // rememberNavBackStack saves the keys, so the stack survives process death.
        val backStack = rememberNavBackStack(LaunchDestination)

        AppNavHost(backStack = backStack)

        // MainViewModel owns no screen of its own — Screen() here only surfaces a session error
        // as a dialog (observeSession() already retries, so this is a last-resort path) and
        // delivers the graph switch.
        Screen(
            viewModel = viewModel,
            isTransparent = true,
            onNavigation = { navigation ->
                when (navigation) {
                    MainNavigation.Main -> backStack.switchTo(HomeDestination)
                    MainNavigation.Auth -> backStack.switchTo(LoginDestination)
                }
            },
        ) { _, _ -> }
    }
}

/**
 * Replaces the whole back stack with [start].
 *
 * Navigation 3's back stack is a plain list, so switching flows is a list operation rather than a
 * `popUpTo(0) { inclusive = true }` incantation.
 */
private fun NavBackStack<NavKey>.switchTo(start: NavKey) {
    clear()
    add(start)
}
