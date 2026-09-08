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
import com.example.androidproject1.feature.auth.presentation.AuthNavGraph
import com.example.androidproject1.feature.home.presentation.MainNavGraph
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.KoinContext

/** The app's only Activity; everything else is a composable destination. */
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

        // The app always starts on the launch screen; MainViewModel decides when to leave it.
        AppNavHost(navController = navController)

        // MainViewModel owns no screen of its own — Screen() here only surfaces a session error
        // as a dialog (observeSession() already retries, so this is a last-resort path) and
        // delivers the graph switch.
        Screen(
            viewModel = viewModel,
            isTransparent = true,
            onNavigation = { navigation ->
                when (navigation) {
                    MainNavigation.Main -> navController.switchGraph(MainNavGraph)
                    MainNavigation.Auth -> navController.switchGraph(AuthNavGraph)
                }
            },
        ) { _, _ -> }
    }
}

/** Replaces the whole back stack with [graph]. */
private fun NavHostController.switchGraph(graph: Any) {
    navigate(graph) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}
