package com.example.androidproject1.service.core.ui.state

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
 * @property loading non-null while a blocking overlay should be shown. Defaults to `null`:
 * a state that is already renderable shows no overlay. `BaseViewModel` passes it explicitly,
 * deriving it from whether `initialState` was null.
 * @property alert non-null while a dialog should be shown over the content.
 * @property content non-null when a failure or empty state should be shown *instead of* the
 * content — see [ContentState].
 */
data class UiState<Data>(
    val data: Data,
    val loading: LoadingState? = null,
    val alert: AlertState? = null,
    val content: ContentState? = null,
)

/**
 * Updates the wrapped [UiState.data] in place, doing nothing while it is still `null`.
 *
 * [action] runs with the current data as its receiver, so the body is the `copy(...)` and nothing
 * else: `uiState.updateData { copy(email = email) }`.
 */
fun <Data> MutableStateFlow<UiState<Data?>>.updateData(action: Data.() -> Data) {
    update { state ->
        val data = state.data ?: return@update state
        state.copy(data = data.action())
    }
}
