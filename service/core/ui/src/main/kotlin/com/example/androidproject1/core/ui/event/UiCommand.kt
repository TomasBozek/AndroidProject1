package com.example.androidproject1.core.ui.event

import com.example.androidproject1.core.ui.text.UiText

/**
 * One-off platform effects any screen may request. Interpreted centrally, so features never touch
 * a Context.
 */
sealed interface UiCommand {

    data object NavigateBack : UiCommand

    data object CloseApp : UiCommand

    data class ShowToast(val message: UiText) : UiCommand

    /**
     * Material 3's preferred way to acknowledge something, and the only one that can carry an
     * action. Rendered by `Screen()`'s own SnackbarHost, so a feature needs no Scaffold of its own.
     *
     * @param actionLabel shows an action button when non-null.
     * @param onAction invoked when that button is pressed; ignored without an [actionLabel].
     */
    data class ShowSnackbar(
        val message: UiText,
        val actionLabel: UiText? = null,
        val withDismissAction: Boolean = false,
        val onAction: () -> Unit = {},
    ) : UiCommand

    data class OpenBrowser(val url: String) : UiCommand

    data object OpenAppSettings : UiCommand
}
