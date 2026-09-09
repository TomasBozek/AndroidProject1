package com.example.androidproject1.feature.settings.presentation.settings

import androidx.compose.runtime.Immutable
import com.example.androidproject1.feature.settings.domain.ThemePreference

@Immutable
data class SettingsState(
    val email: String?,
    /**
     * Whether this build has a debug menu (D16). Not something a ViewModel can know: it is a
     * property of the build, so `:app` reports it through `SettingsDestination`.
     */
    val debugMenuEnabled: Boolean = false,
    /** Which palette the app draws in. `System` until the stored choice has been read. */
    val theme: ThemePreference = ThemePreference.DEFAULT,
) {

    companion object {

        val PREVIEW = SettingsState(
            email = "ada@example.com",
            debugMenuEnabled = true,
            theme = ThemePreference.System,
        )
    }
}
