package com.example.androidproject1

import android.content.Intent
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

    // internal rather than private: `MainActivityDeepLinkTest` recreates this activity and asserts
    // that the launch intent was not read a second time, which is only visible on the ViewModel
    // that outlived the recreation.
    internal val viewModel by viewModel<MainViewModel>()

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

        // Only on a real cold start. `onCreate` runs again on every rotation and after process
        // death with the same launch intent still attached, and re-reading it there re-applied the
        // link — the synthesised path was rebuilt and whatever the user had navigated to since was
        // thrown away. A non-null `savedInstanceState` is exactly "this is a recreation", and the
        // back stack that comes back with it already holds the link's own keys.
        //
        // `coldStart = true`: the app was launched by this link, so there is no back stack
        // behind the product and one has to be synthesised or Up closes the app.
        if (savedInstanceState == null) {
            viewModel.onDeepLink(intent.deepLinkUri(), coldStart = true)
        }

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

                // Applied after the flow has put its root on the stack, and only in the
                // signed-in one: a link followed while signed out lands on Login, and the
                // pending keys wait rather than being dropped.
                val deepLink by viewModel.deepLink.collectAsStateWithLifecycle()
                LaunchedEffect(deepLink, session) {
                    if (deepLink.isNotEmpty() && session == SessionState.SignedIn) {
                        backStack.applyDeepLink(deepLink)
                        viewModel.onDeepLinkApplied()
                    }
                }

                // Nothing to display until the session has named a flow, or a saved stack has
                // come back. The splash screen is what the user sees until then.
                if (backStack.isNotEmpty()) AppNavHost(backStack = backStack)
            }
        }
    }

    /**
     * A link that arrived while the app was already running.
     *
     * `coldStart = false`: there is a back stack, and the user's place in it is theirs. The
     * product is pushed onto whatever tab they were on, so Up returns them to it.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.onDeepLink(intent.deepLinkUri(), coldStart = false)
    }

    /**
     * The link this intent carries, if it is one of ours.
     *
     * The scheme is checked here, against the same `applicationId` the manifest's filter is
     * built from, so there is one place that says what the app's scheme is and nothing to keep
     * in step. `DeepLinks` then parses a plain string and stays an ordinary JVM function.
     */
    private fun Intent.deepLinkUri(): String? =
        data?.takeIf { it.scheme == BuildConfig.APPLICATION_ID }?.toString()
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

/**
 * Puts a link's keys on the stack.
 *
 * A cold start hands over the whole path and replaces what is there — which is the flow's root
 * and nothing else. A warm one hands over the target alone and it is pushed, so the user's
 * place is kept and Up returns them to it. Either way the last key is not added twice when it
 * is already on top, which is what a second tap on the same link would otherwise do.
 */
private fun NavBackStack<NavKey>.applyDeepLink(keys: List<NavKey>) {
    if (keys.isEmpty() || lastOrNull() == keys.last()) return
    if (keys.size > 1) clear()
    addAll(keys)
}
