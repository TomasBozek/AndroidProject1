package com.example.androidproject1.feature.onboarding.presentation.onboarding

import com.example.androidproject1.core.ui.event.UiEvent

sealed interface OnboardingEvent : UiEvent {

    /**
     * The pager settled on a page.
     *
     * A swipe is something the user did, so it arrives as an event like any other: the pager is
     * the only thing that knows a finger moved, and the ViewModel stays the only owner of where
     * the tour is.
     */
    data class PageChanged(val page: Int) : OnboardingEvent

    /** Advances a page, or finishes the tour on the last one. */
    data object NextClicked : OnboardingEvent

    /** Finishes the tour from wherever it is. */
    data object SkipClicked : OnboardingEvent
}
