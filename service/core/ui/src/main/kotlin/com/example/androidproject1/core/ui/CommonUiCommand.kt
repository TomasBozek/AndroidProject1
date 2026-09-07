package com.example.androidproject1.core.ui

/**
 * One-off platform effects that any screen may request. They are interpreted centrally in
 * [com.example.androidproject1.core.ui.component.Screen], so features never touch a Context.
 */
sealed interface CommonUiCommand {

    data object PressBack : CommonUiCommand

    data object CloseApp : CommonUiCommand

    data class ShowToast(val message: AppString) : CommonUiCommand

    sealed interface OpenUri : CommonUiCommand {

        data class ExternalBrowser(val uri: String) : OpenUri

        data object AppSettings : OpenUri
    }
}
