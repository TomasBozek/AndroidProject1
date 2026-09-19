package com.example.androidproject1.feature.movies.presentation.movies

/** One-off navigation intents, turned into back-stack calls in MoviesDestination. */
sealed interface MoviesNavigation {

    data class MovieDetail(val id: Int) : MoviesNavigation

    data object NavigateUp : MoviesNavigation
}
