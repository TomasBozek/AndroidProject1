package com.example.androidproject1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.androidproject1.core.ui.component.Screen
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.core.ui.util.CommandEffect
import com.example.androidproject1.feature.auth.presentation.AuthNavGraph
import com.example.androidproject1.feature.home.presentation.MainNavGraph
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.KoinContext

/**
 * The app's only Activity. Everything else is a composable destination.
 */
class MainActivity : ComponentActivity() {

    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KoinContext {
                AppTheme {
                    MainContent()
                }
            }
        }
    }

    @Composable
    private fun MainContent() {
        val navController = rememberNavController()

        // Screen() renders nothing until state.data is non-null, so the NavHost is not created
        // until the session is known — which is what stops the login screen flashing on a cold
        // start while already signed in.
        Screen(viewModel = viewModel) { state, _ ->
            AppNavHost(
                navController = navController,
                startGraph = if (state.isLoggedIn) MainNavGraph else AuthNavGraph,
            )
        }

        // A session change can land while the UI is below STARTED; BaseViewModel buffers
        // directions in a channel, so this collector picks it up on resume rather than missing it.
        CommandEffect(commandFlow = viewModel.direction) { direction ->
            when (direction) {
                MainDirection.Main -> navController.switchGraph(MainNavGraph)
                MainDirection.Auth -> navController.switchGraph(AuthNavGraph)
            }
        }
    }
}

/** Replaces the whole back stack with [graph]. */
private fun NavHostController.switchGraph(graph: Any) {
    navigate(graph) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}
