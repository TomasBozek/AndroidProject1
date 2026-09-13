package com.example.androidproject1.service.core.ui.event

import com.example.androidproject1.service.core.ui.text.UiText

/**
 * One-off platform effects any screen may request. Interpreted centrally, so features never touch
 * a Context.
 */
sealed interface UiCommand {

    data object NavigateBack : UiCommand

    data object CloseApp : UiCommand

    /**
     * The one way to acknowledge something in passing — a toast is this with no action (D63).
     * Rendered by `Screen()`'s own SnackbarHost through the chrome, so it wears the theme, sits
     * inside the screen test and needs no Scaffold of its own.
     *
     * @param id identifies this snackbar when its action comes back as
     * [SystemEvent.SnackbarAction]. Every command is plain data — pressing the action raises an
     * event like any other button, rather than invoking a lambda the command carried.
     * @param actionLabel shows an action button when non-null.
     */
    data class ShowSnackbar(
        val id: String,
        val message: UiText,
        val actionLabel: UiText? = null,
        val withDismissAction: Boolean = false,
    ) : UiCommand

    data class OpenBrowser(val url: String) : UiCommand

    data object OpenAppSettings : UiCommand
}
