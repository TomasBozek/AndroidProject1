package com.example.androidproject1.feature.home.presentation

import kotlinx.serialization.Serializable

/**
 * Nav graph shown while signed in. Home is its start destination, which is why the marker lives
 * here; the graph's contents are composed in the app module's `AppNavHost`.
 */
@Serializable
data object MainNavGraph
