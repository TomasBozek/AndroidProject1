package com.example.androidproject1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.auth.presentation.login.LoginDestination
import com.example.androidproject1.feature.home.presentation.home.HomeDestination
import com.example.androidproject1.feature.onboarding.presentation.onboarding.OnboardingDestination
import com.example.androidproject1.feature.settings.domain.ThemePreference
import org.koin.androidx.viewmodel.ext.android.viewModel

/** The app's only Activity; everything else is a composable destination. */
class MainActivity : ComponentActivity() {

    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Before super.onCreate, so the system splash screen is installed before the first frame.
        // It stays up for exactly as long as reading the session takes — there is no minimum hold,
        // and nothing to tune if that read ever grows into real startup work.
        // Both reads, not just the session: the theme decides what the first frame is painted
        // in, so drawing before it lands is a light flash in front of someone who chose dark.
        installSplashScreen().setKeepOnScreenCondition {
            viewModel.sessionState.value == SessionState.Unknown || viewModel.theme.value == null
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val theme by viewModel.theme.collectAsStateWithLifecycle()

            // No KoinContext wrapper: since Koin 4.2, startKoin() sets the Compose context up.
            AppTheme(darkTheme = theme.isDark()) {
                val session by viewModel.sessionState.collectAsStateWithLifecycle()

                // Starts empty and is filled once the session says which flow the user is in — and
                // must be composed on the very first frame either way: rememberNavBackStack is a
                // rememberSaveable, and one that first enters composition on a later frame gets
                // nothing back from the restored state. Composing it behind the session is how the
                // back stack was lost after process death.
                val backStack = rememberNavBackStack()

                // Which flow the user is in *is* the identity of the stack's first key, so a
                // session change is applied by asserting that rather than by remembering whether it
                // has already been applied. A stack restored into the matching flow is left alone.
                val rootKey = session.rootKey()
                LaunchedEffect(rootKey) {
                    if (rootKey != null && backStack.firstOrNull() != rootKey) {
                        backStack.switchTo(rootKey)
                    }
                }

                // Nothing to display until the session has named a flow, or a saved stack has come
                // back. The splash screen is what the user sees until then.
                if (backStack.isNotEmpty()) AppNavHost(backStack = backStack)
            }
        }
    }
}

/**
 * Whether to draw dark, for a preference that may not have been read yet.
 *
 * `null` and [ThemePreference.System] answer the same way — follow the device — so an
 * unfinished read looks like the default rather than like a choice nobody made.
 */
@Composable
private fun ThemePreference?.isDark(): Boolean = when (this) {
    ThemePreference.Light -> false
    ThemePreference.Dark -> true
    ThemePreference.System, null -> isSystemInDarkTheme()
}

/** The first key of the flow this state belongs in, or `null` while it is not known yet. */
private fun SessionState.rootKey(): NavKey? = when (this) {
    SessionState.Unknown -> null
    SessionState.Onboarding -> OnboardingDestination
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
