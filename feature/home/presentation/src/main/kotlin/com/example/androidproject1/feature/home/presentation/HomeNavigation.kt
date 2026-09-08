package com.example.androidproject1.feature.home.presentation

sealed interface HomeNavigation {

    data object Settings : HomeNavigation

    data object Catalog : HomeNavigation
}
