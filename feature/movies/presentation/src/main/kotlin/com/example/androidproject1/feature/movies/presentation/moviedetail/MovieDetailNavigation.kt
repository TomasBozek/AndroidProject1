package com.example.androidproject1.feature.movies.presentation.moviedetail

/** One-off navigation intents, turned into back-stack calls in MovieDetailDestination. */
sealed interface MovieDetailNavigation {

    data object NavigateUp : MovieDetailNavigation
}
