package com.example.androidproject1.feature.settings.presentation.settings

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
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppSectionHeader
import com.example.androidproject1.core.ui.component.AppSegmented
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.component.ButtonKind
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.settings.domain.ThemePreference
import com.example.androidproject1.feature.settings.presentation.R

@Composable
fun SettingsScreen(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
) {
    AppScaffold(
        screenId = "SettingsScreen",
        // No up arrow: this is a tab root, and the bottom bar is what leaves it.
        topBar = { AppTopBar(title = stringResource(R.string.settings_title)) },
    ) {
        Column(
            // Scrollable since the theme section joined it: the list is taller than a small
            // phone's window, and a settings entry that cannot be reached is not there.
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
        ) {
            AppText(
                text = stringResource(R.string.settings_signed_in_as),
                role = TextRole.LabelSmall,
            )
            AppText(
                text = state.email ?: stringResource(R.string.settings_no_email),
                role = TextRole.BodyLarge,
                modifier = Modifier.testTag("settings_emailValue"),
            )

            // Three options that switch immediately: a segmented control rather than a
            // switch, because `System` is a choice of its own and not the absence of one.
            AppSectionHeader(title = stringResource(R.string.settings_theme))
            AppSegmented(
                options = ThemePreference.entries.map { it.label() },
                selectedIndex = state.theme.ordinal,
                onSelect = { index ->
                    onEvent(SettingsEvent.ThemeSelected(ThemePreference.entries[index]))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_themeTab"),
            )

            AppButton(
                label = stringResource(R.string.settings_profile),
                onClick = { onEvent(SettingsEvent.ProfileClicked) },
                kind = ButtonKind.Outline,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_profileButton"),
            )

            // Only dev and staging have one (D16); a prod build reports `false` and the entry
            // is not drawn. The component gallery lives behind it now.
            if (state.debugMenuEnabled) {
                AppButton(
                    label = stringResource(R.string.settings_debug_menu),
                    onClick = { onEvent(SettingsEvent.DebugMenuClicked) },
                    kind = ButtonKind.Outline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_debugMenuButton"),
                )
            }

            AppButton(
                label = stringResource(R.string.settings_permissions),
                onClick = { onEvent(SettingsEvent.PermissionsClicked) },
                kind = ButtonKind.Outline,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_permissionsButton"),
            )

            // Signing out is not destructive here — the confirm dialog is what guards it.
            AppButton(
                label = stringResource(R.string.settings_logout),
                onClick = { onEvent(SettingsEvent.LogoutClicked) },
                kind = ButtonKind.Neutral,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_logoutButton"),
            )
        }
    }
}

/** The enum is the order the control draws in, so the labels are looked up rather than listed. */
@Composable
private fun ThemePreference.label(): String = stringResource(
    when (this) {
        ThemePreference.System -> R.string.settings_theme_system
        ThemePreference.Light -> R.string.settings_theme_light
        ThemePreference.Dark -> R.string.settings_theme_dark
    },
)

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    SettingsScreen(
        state = SettingsState.PREVIEW,
    ) {}
}
