package com.example.androidproject1.core.ui.state

import com.example.androidproject1.core.ui.AppString
import com.example.androidproject1.service.core.ui.R
import com.example.androidproject1.core.ui.toText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Envelope wrapping every screen's state.
 *
 * Loading and alerts are deliberately *not* part of a feature's own state class: they are rendered
 * centrally by [com.example.androidproject1.core.ui.component.Screen], so no feature has to
 * reimplement a spinner or an error dialog.
 *
 * @property data the feature's own state; `null` until the ViewModel has produced it.
 * @property loading non-null while a blocking loading overlay should be shown.
 * @property alert non-null while an alert dialog should be shown.
 */
data class UiState<Data>(
    val data: Data,
    val loading: ModalLoadingState? = ModalLoadingState(),
    val alert: AlertState? = null,
)

data class ModalLoadingState(
    val message: AppString = R.string.core_general_loading.toText(),
)

/**
 * @property id identifies which alert this is, so a ViewModel showing several dialogs can tell
 * the resulting [com.example.androidproject1.core.ui.CommonEvent.AlertDialogAction]s apart.
 * @property data arbitrary payload echoed back with the action.
 */
data class AlertState(
    val id: String,
    val title: AppString? = null,
    val message: AppString?,
    val primaryButton: AppString = R.string.core_general_dismiss.toText(),
    val secondaryButton: AppString? = null,
    val dismissible: Boolean = true,
    val data: Map<String, Any> = emptyMap(),
)

/** Updates the wrapped [UiState.data], doing nothing while it is still `null`. */
fun <Data> MutableStateFlow<UiState<Data?>>.updateData(action: Data.(Data) -> Data) {
    update { state ->
        val data = state.data ?: return@update state
        state.copy(data = data.action(data))
    }
}

var <Data> MutableStateFlow<UiState<Data>>.isLoading: Boolean
    get() = value.loading != null
    set(value) = update { it.copy(loading = if (value) ModalLoadingState() else null) }

fun <Data> MutableStateFlow<UiState<Data>>.updateAlert(action: (AlertState?) -> AlertState?) {
    update { it.copy(alert = action(it.alert)) }
}

fun <Data> MutableStateFlow<UiState<Data>>.clearAlert() {
    update { it.copy(alert = null) }
}

fun <Data> MutableStateFlow<UiState<Data>>.setAlert(
    id: String,
    message: AppString,
    title: AppString? = null,
    primaryButton: AppString = R.string.core_general_dismiss.toText(),
    secondaryButton: AppString? = null,
    dismissible: Boolean = true,
    data: Map<String, Any> = emptyMap(),
) {
    updateAlert {
        AlertState(
            id = id,
            title = title,
            message = message,
            primaryButton = primaryButton,
            secondaryButton = secondaryButton,
            dismissible = dismissible,
            data = data,
        )
    }
}
