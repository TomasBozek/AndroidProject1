package com.example.androidproject1.feature.movies.presentation.movies

import com.example.androidproject1.service.core.ui.event.UiEvent

sealed interface MoviesEvent : UiEvent {

    /** The list was pulled down. */
    data object Refresh : MoviesEvent

    /** The list reached the end of what it has. */
    data object LoadMore : MoviesEvent

    data class MovieClicked(val id: Int) : MoviesEvent

    data object NavigateUpClicked : MoviesEvent
}
