package com.example.androidproject1.feature.settings.presentation.settings

import androidx.compose.runtime.Immutable

@Immutable
data class SettingsState(
    val email: String?,
    /**
     * Whether this build has a debug menu (D16). Not something a ViewModel can know: it is a
     * property of the build, so `:app` reports it through `SettingsDestination`.
     */
    val debugMenuEnabled: Boolean = false,
) {

    companion object {

        val PREVIEW = SettingsState(
            email = "ada@example.com",
            debugMenuEnabled = true,
        )
    }
}
