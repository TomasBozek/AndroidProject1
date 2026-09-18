package com.example.androidproject1.feature.devmenu.presentation.playground

/** One-off navigation intents, turned into back-stack calls in DevMenuPlaygroundDestination. */
sealed interface DevMenuPlaygroundNavigation {

    data object NavigateUp : DevMenuPlaygroundNavigation
}
