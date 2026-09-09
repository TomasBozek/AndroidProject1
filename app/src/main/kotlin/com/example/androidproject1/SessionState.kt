package com.example.androidproject1

import androidx.compose.runtime.Immutable

/**
 * Which of the app's three flows is the right one, as far as it knows.
 *
 * [Unknown] is not a loading flag with a nicer name: it is the state the app genuinely starts in,
 * before the stored session and the onboarding flag have been read once. The splash screen stays
 * up while it holds, so no flow is composed until the right one is known and nothing has to be
 * swapped a frame later.
 *
 * [Onboarding] outranks the other two: someone who has not finished the tour is shown it whether
 * or not a session is stored, because a stored session on a first run means the app was
 * reinstalled over one, not that the tour was taken.
 */
@Immutable
sealed interface SessionState {

    /** The session and the onboarding flag have not both been read yet. */
    data object Unknown : SessionState

    /** The first-run tour has not been finished. */
    data object Onboarding : SessionState

    data object SignedIn : SessionState

    data object SignedOut : SessionState
}
