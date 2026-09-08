package com.example.androidproject1

/**
 * Whether anyone is signed in, as far as the app knows.
 *
 * [Unknown] is not a loading flag with a nicer name: it is the state the app genuinely starts in,
 * before the stored session has been read once. The splash screen stays up while it holds, so no
 * flow is composed until the right one is known and nothing has to be swapped a frame later.
 */
sealed interface SessionState {

    /** The session has not been read yet. */
    data object Unknown : SessionState

    data object SignedIn : SessionState

    data object SignedOut : SessionState
}
