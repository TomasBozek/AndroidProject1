package com.example.androidproject1

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.navigation
import com.example.androidproject1.feature.auth.presentation.AuthNavGraph
import com.example.androidproject1.feature.auth.presentation.LoginDestination
import com.example.androidproject1.feature.auth.presentation.loginDestination
import com.example.androidproject1.feature.home.presentation.HomeDestination
import com.example.androidproject1.feature.home.presentation.MainNavGraph
import com.example.androidproject1.feature.home.presentation.homeDestination
import com.example.androidproject1.feature.settings.presentation.SettingsDestination
import com.example.androidproject1.feature.settings.presentation.settingsDestination

/**
 * The app's single navigation graph, and the only place that knows about more than one feature.
 *
 * Cross-feature navigation is passed into each destination as a lambda (see [homeDestination]),
 * which is what lets feature presentation modules stay independent of one another.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    startGraph: Any,
) {
    NavHost(
        navController = navController,
        startDestination = startGraph,
    ) {
        navigation<AuthNavGraph>(startDestination = LoginDestination) {
            loginDestination(navController = navController)
        }

        navigation<MainNavGraph>(startDestination = HomeDestination) {
            homeDestination(
                navController = navController,
                navigateToSettings = { navController.navigate(SettingsDestination) },
            )
            settingsDestination(navController = navController)
        }
    }
}
