package com.example.androidproject1.core.ui.state

import com.example.androidproject1.core.ui.text.UiText
import com.example.androidproject1.core.ui.text.toUiText
import com.example.androidproject1.service.core.ui.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Shown *instead of* a screen's content: the call failed, or it succeeded and there is nothing to
 * show.
 *
 * Distinct from [AlertState], which interrupts a screen that is otherwise fine, and from
 * [LoadingState], which covers it while work is in flight. Rendered centrally by `Screen()`, so no
 * feature builds its own "couldn't load, try again" layout.
 *
 * @property id which content state this is, echoed back with the action so a screen showing more
 * than one can tell them apart.
 */
sealed interface ContentState {

    val id: String

    val title: UiText?

    val message: UiText

    /** Label for the action button, or `null` for a state the user cannot act on. */
    val actionLabel: UiText?

    /**
     * The call failed. The action re-runs it — see `BaseViewModel.execute`'s `errorDisplay`, which
     * remembers the failed call so a subclass does not have to.
     */
    data class Error(
        override val id: String = DEFAULT_ID,
        override val title: UiText? = R.string.core_alert_error_title.toUiText(),
        override val message: UiText,
        override val actionLabel: UiText? = R.string.core_action_retry.toUiText(),
    ) : ContentState

    /** The call succeeded and returned nothing. */
    data class Empty(
        override val id: String = DEFAULT_ID,
        override val title: UiText? = null,
        override val message: UiText = R.string.core_empty_message.toUiText(),
        override val actionLabel: UiText? = null,
    ) : ContentState

    companion object {

        const val DEFAULT_ID = "content"
    }
}

fun <Data> MutableStateFlow<UiState<Data>>.setContent(state: ContentState) {
    update { it.copy(content = state) }
}

fun <Data> MutableStateFlow<UiState<Data>>.clearContent() {
    update { it.copy(content = null) }
}
