package com.example.androidproject1.core.ui.state

import com.example.androidproject1.core.ui.text.UiText
import com.example.androidproject1.core.ui.text.toUiText
import com.example.androidproject1.service.core.ui.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Context an alert carries back to the ViewModel with its result — "which item was I confirming?".
 *
 * A feature implements it with its own data class, so the confirm handler reads a typed value
 * instead of casting out of a map:
 *
 * ```
 * private data class DeleteItem(val id: String) : AlertPayload
 * ```
 */
interface AlertPayload

/**
 * @property id identifies which alert this is, so a ViewModel showing several dialogs can tell the
 * resulting [com.example.androidproject1.core.ui.event.SystemEvent.AlertResult]s apart.
 * @property payload optional typed context echoed back with the result.
 */
data class AlertState(
    val id: String,
    val title: UiText? = null,
    val message: UiText?,
    val confirmLabel: UiText = R.string.core_action_ok.toUiText(),
    val declineLabel: UiText? = null,
    val dismissible: Boolean = true,
    val payload: AlertPayload? = null,
)

fun <Data> MutableStateFlow<UiState<Data>>.clearAlert() {
    update { it.copy(alert = null) }
}

fun <Data> MutableStateFlow<UiState<Data>>.setAlert(
    id: String,
    message: UiText,
    title: UiText? = null,
    confirmLabel: UiText = R.string.core_action_ok.toUiText(),
    declineLabel: UiText? = null,
    dismissible: Boolean = true,
    payload: AlertPayload? = null,
) {
    update {
        it.copy(
            alert = AlertState(
                id = id,
                title = title,
                message = message,
                confirmLabel = confirmLabel,
                declineLabel = declineLabel,
                dismissible = dismissible,
                payload = payload,
            ),
        )
    }
}
