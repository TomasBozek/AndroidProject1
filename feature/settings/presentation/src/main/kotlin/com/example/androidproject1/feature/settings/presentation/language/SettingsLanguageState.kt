package com.example.androidproject1.feature.settings.presentation.language

import androidx.compose.runtime.Immutable
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.androidproject1.feature.settings.domain.AppLanguage

@Immutable
data class SettingsLanguageState(
    /** The stored choice. [AppLanguage.DEFAULT] until the store has been read. */
    val selected: AppLanguage = AppLanguage.DEFAULT,
) {

    companion object {

        val PREVIEW = SettingsLanguageState(selected = AppLanguage.System)
    }
}

/** One preview per option, so each ring is seen filled once. */
class SettingsLanguageStatePreviews : PreviewParameterProvider<SettingsLanguageState> {

    override val values = AppLanguage.entries.asSequence().map { SettingsLanguageState(selected = it) }
}
