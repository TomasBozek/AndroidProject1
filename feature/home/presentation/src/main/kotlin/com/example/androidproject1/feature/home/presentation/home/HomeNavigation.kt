package com.example.androidproject1.feature.home.presentation.home

/**
 * Home is a tab root, so the bottom bar is what leaves it — except for Inventory and Movies, which
 * have no tab of their own (five is Material's ceiling, D59) and are reached from the cards here.
 * Wired as lambdas in `AppNavHost`, the way Settings reaches Profile.
 */
sealed interface HomeNavigation {

    data object OpenInventory : HomeNavigation

    data object OpenMovies : HomeNavigation
}
