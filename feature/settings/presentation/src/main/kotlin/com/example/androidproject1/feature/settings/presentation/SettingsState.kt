package com.example.androidproject1.feature.settings.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class SettingsState(
    val email: String?,
) {

    companion object {

        val PREVIEW = SettingsState(
            email = "ada@example.com",
        )
    }
}
