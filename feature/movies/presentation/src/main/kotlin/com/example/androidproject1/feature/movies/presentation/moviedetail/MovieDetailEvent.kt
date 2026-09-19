package com.example.androidproject1.feature.movies.presentation.moviedetail

import com.example.androidproject1.service.core.ui.event.UiEvent

sealed interface MovieDetailEvent : UiEvent {

    data object NavigateUpClicked : MovieDetailEvent
}
