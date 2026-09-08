package com.example.androidproject1

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.navigation
import com.example.androidproject1.feature.auth.presentation.AuthNavGraph
import com.example.androidproject1.feature.auth.presentation.LoginDestination
import com.example.androidproject1.feature.auth.presentation.loginDestination
import com.example.androidproject1.feature.auth.presentation.signUpDestination
import com.example.androidproject1.feature.catalog.presentation.CategoriesDestination
import com.example.androidproject1.feature.catalog.presentation.categoriesDestination
import com.example.androidproject1.feature.catalog.presentation.productDetailDestination
import com.example.androidproject1.feature.catalog.presentation.productsDestination
import com.example.androidproject1.feature.home.presentation.HomeDestination
import com.example.androidproject1.feature.home.presentation.MainNavGraph
import com.example.androidproject1.feature.home.presentation.homeDestination
import com.example.androidproject1.feature.launch.presentation.LaunchDestination
import com.example.androidproject1.feature.launch.presentation.launchDestination
import com.example.androidproject1.feature.settings.presentation.SettingsDestination
import com.example.androidproject1.feature.settings.presentation.settingsDestination

/**
 * The only place that knows about more than one feature. Cross-feature navigation is a lambda.
 *
 * Always starts at [LaunchDestination] — MainViewModel is what decides, once the session is
 * known, whether to swap it for [AuthNavGraph] or [MainNavGraph].
 */
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = LaunchDestination,
    ) {
        launchDestination()

        navigation<AuthNavGraph>(startDestination = LoginDestination) {
            loginDestination(navController = navController)
            signUpDestination(navController = navController)
        }

        navigation<MainNavGraph>(startDestination = HomeDestination) {
            homeDestination(
                navController = navController,
                navigateToSettings = { navController.navigate(SettingsDestination) },
                navigateToCatalog = { navController.navigate(CategoriesDestination) },
            )
            settingsDestination(navController = navController)
            categoriesDestination(navController = navController)
            productsDestination(navController = navController)
            productDetailDestination(navController = navController)
        }
    }
}
