package com.example.androidproject1.feature.home.presentation.home

/**
 * Home is a tab root, so the bottom bar is what leaves it — except for Inventory, which has no
 * tab of its own (five is Material's ceiling, D59) and is reached from the card here. Wired as a
 * lambda in `AppNavHost`, the way Settings reaches Profile.
 */
sealed interface HomeNavigation {

    data object OpenInventory : HomeNavigation
}
