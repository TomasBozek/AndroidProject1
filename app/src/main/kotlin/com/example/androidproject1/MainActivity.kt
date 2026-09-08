package com.example.androidproject1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
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
        val session by viewModel.sessionState.collectAsStateWithLifecycle()

        // The app always starts on the launch screen; the session decides when to leave it.
        // rememberNavBackStack saves the keys, so the stack survives process death.
        val backStack = rememberNavBackStack(LaunchDestination)

        // Which flow the user is in is the identity of the stack's first key, so the session is
        // applied by asserting that rather than by tracking whether it has already been applied.
        // Nothing to do while the session is Unknown — the launch screen is already up.
        val rootKey = session.rootKey()
        LaunchedEffect(rootKey) {
            if (rootKey != null && backStack.firstOrNull() != rootKey) backStack.switchTo(rootKey)
        }

        AppNavHost(backStack = backStack)
    }
}

/** The first key of the flow this session belongs in, or `null` while it is not known yet. */
private fun SessionState.rootKey(): NavKey? = when (this) {
    SessionState.Unknown -> null
    SessionState.SignedIn -> HomeDestination
    SessionState.SignedOut -> LoginDestination
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
