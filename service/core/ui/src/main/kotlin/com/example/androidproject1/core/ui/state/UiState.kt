package com.example.androidproject1.core.ui.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Envelope wrapping every screen's state.
 *
 * Loading, alerts and empty/error content are deliberately not part of a feature's own state class:
 * they are rendered centrally, so no feature reimplements a spinner, an error dialog or a
 * "couldn't load" layout.
 *
 * @property data the feature's own state; `null` until the ViewModel has produced it.
 * @property loading non-null while a blocking overlay should be shown.
 * @property alert non-null while a dialog should be shown over the content.
 * @property content non-null when a failure or empty state should be shown *instead of* the
 * content — see [ContentState].
 */
data class UiState<Data>(
    val data: Data,
    val loading: LoadingState? = LoadingState(),
    val alert: AlertState? = null,
    val content: ContentState? = null,
)

/** Updates the wrapped [UiState.data], doing nothing while it is still `null`. */
fun <Data> MutableStateFlow<UiState<Data?>>.updateData(action: Data.(Data) -> Data) {
    update { state ->
        val data = state.data ?: return@update state
        state.copy(data = data.action(data))
    }
}
