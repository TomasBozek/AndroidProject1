package com.example.androidproject1.service.core.ui.form

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import com.example.androidproject1.service.core.ui.R
import com.example.androidproject1.service.core.ui.state.AlertState
import com.example.androidproject1.service.core.ui.text.toUiText
import kotlin.coroutines.cancellation.CancellationException

/** The id every discard confirmation is raised and answered under. */
const val ALERT_ID_DISCARD = "discard_changes"

/**
 * The confirmation a form raises when the back gesture would throw away what has been typed.
 *
 * One wording and one id for every form in the app, in `:service:core:ui` because the copy ships
 * with the module: three screens asking the same question three different ways is how a product
 * stops sounding like one thing.
 */
fun discardAlert(): AlertState = AlertState(
    id = ALERT_ID_DISCARD,
    title = R.string.core_discard_title.toUiText(),
    message = R.string.core_discard_message.toUiText(),
    confirmLabel = R.string.core_action_discard.toUiText(),
    declineLabel = R.string.core_action_keep_editing.toUiText(),
)

/**
 * Intercepts the back gesture while [dirty], and runs [onBack] when it completes.
 *
 * `PredictiveBackHandler` rather than `BackHandler` so the gesture still animates: the swipe is
 * followed, and only a *finished* one asks the question. A cancelled swipe — the one where a thumb
 * changes its mind halfway — throws `CancellationException` out of the collect and must ask
 * nothing, which is the whole reason this is not a one-line `BackHandler`.
 *
 * The question itself is an [AlertState] the view model raises, so it is drawn by `Screen()` like
 * every other alert and the screen adds no dialog of its own.
 */
@Composable
fun DiscardBackHandler(dirty: Boolean, onBack: () -> Unit) {
    PredictiveBackHandler(enabled = dirty) { progress ->
        try {
            progress.collect { }
            onBack()
        } catch (_: CancellationException) {
            // The gesture was abandoned. Nothing was going to be lost, so nothing is asked.
        }
    }
}
