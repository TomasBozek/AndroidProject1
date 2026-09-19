package com.example.androidproject1.feature.settings.presentation.language

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppDivider
import com.example.androidproject1.core.ui.component.AppRadio
import com.example.androidproject1.core.ui.component.AppRadioGroup
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.settings.domain.AppLanguage
import com.example.androidproject1.feature.settings.presentation.R

/**
 * The per-app language: the locales the app ships, plus the device's own. A radio list rather
 * than a segmented control like the theme's, because a language name is long in every language
 * and a fourth locale would not fit a segment. Drawn first:
 * <https://claude.ai/artifact/3zJBaghVcsxwSnitHbyCDu>.
 */
@Composable
fun SettingsLanguageScreen(
    state: SettingsLanguageState,
    onEvent: (SettingsLanguageEvent) -> Unit,
) {
    AppScaffold(
        screenId = "SettingsLanguageScreen",
        topBar = {
            AppTopBar(
                title = stringResource(R.string.settings_language_title),
                onNavigateUp = { onEvent(SettingsLanguageEvent.NavigateUpClicked) },
                navigateUpTestTag = "settingsLanguage_upButton",
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
        ) {
            AppText(
                text = stringResource(R.string.settings_language_intro),
                role = TextRole.Secondary,
            )
            // The enum is the order the list draws in, so the options are `entries` and each
            // row's tag is built from the option's own tag — `systemItem`, `enItem`, `csItem`.
            AppRadioGroup(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settingsLanguage_optionsGroup"),
            ) {
                AppLanguage.entries.forEach { language ->
                    val stem = language.tag.ifEmpty { "system" }
                    AppRadio(
                        selected = language == state.selected,
                        onSelect = { onEvent(SettingsLanguageEvent.LanguageSelected(language)) },
                        label = language.label(),
                        modifier = Modifier.testTag("settingsLanguage_${stem}Item"),
                    )
                }
            }
            AppDivider()
            AppText(
                text = stringResource(R.string.settings_language_note),
                role = TextRole.Secondary,
            )
        }
    }
}

/**
 * A language is named in itself — "English", "Čeština" — in every locale, which is how a person
 * who cannot read the current one finds theirs; only "follow the device" is translated.
 */
@Composable
private fun AppLanguage.label(): String = stringResource(
    when (this) {
        AppLanguage.System -> R.string.settings_language_system
        AppLanguage.English -> R.string.settings_language_english
        AppLanguage.Czech -> R.string.settings_language_czech
    },
)

@ScreenPreview
@Composable
private fun Preview(
    @PreviewParameter(SettingsLanguageStatePreviews::class) state: SettingsLanguageState,
) = ThemedScreenPreview {
    SettingsLanguageScreen(
        state = state,
    ) {}
}
