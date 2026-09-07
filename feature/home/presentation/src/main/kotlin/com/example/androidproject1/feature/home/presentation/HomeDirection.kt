package com.example.androidproject1.feature.home.presentation

sealed interface HomeDirection {

    data object Settings : HomeDirection
}
