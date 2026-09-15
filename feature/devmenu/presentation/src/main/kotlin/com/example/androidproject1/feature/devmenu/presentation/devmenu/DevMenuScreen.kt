package com.example.androidproject1.feature.devmenu.presentation.devmenu

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
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppDescriptionList
import com.example.androidproject1.core.ui.component.AppListItem
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppSectionHeader
import com.example.androidproject1.core.ui.component.AppSwitch
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.component.ButtonKind
import com.example.androidproject1.core.ui.component.DescriptionRow
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.devmenu.presentation.R
import com.example.androidproject1.service.core.ui.text.resolve

/**
 * What a tester needs and nobody else should see: which build this is, what it talks to, who is
 * signed in, a jump to any screen worth reaching without walking the app to it, and the two
 * switches that are otherwise a `touch` over adb.
 *
 * Only `dev` and `staging` reach it — see `DebugMenu.ENABLED` in `:app`'s flavor source sets (D16).
 */
@Composable
fun DevMenuScreen(
    state: DevMenuState,
    onEvent: (DevMenuEvent) -> Unit,
) {
    AppScaffold(
        screenId = "DevMenuScreen",
        topBar = {
            AppTopBar(
                title = stringResource(R.string.dev_menu_title),
                onNavigateUp = { onEvent(DevMenuEvent.NavigateUpClicked) },
                navigateUpTestTag = "devMenu_upButton",
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
        ) {
            AppSectionHeader(title = stringResource(R.string.dev_menu_build))
            AppDescriptionList(
                modifier = Modifier.testTag("devMenu_buildList"),
                rows = listOf(
                    DescriptionRow(
                        label = stringResource(R.string.dev_menu_flavor),
                        value = "${state.build.flavor} · ${state.build.buildType}",
                    ),
                    DescriptionRow(
                        label = stringResource(R.string.dev_menu_application_id),
                        value = state.build.applicationId,
                    ),
                    DescriptionRow(
                        label = stringResource(R.string.dev_menu_version),
                        value = "${state.build.versionName} (${state.build.versionCode})",
                    ),
                    DescriptionRow(
                        label = stringResource(R.string.dev_menu_base_url),
                        value = state.build.baseUrl,
                    ),
                    DescriptionRow(
                        label = stringResource(R.string.dev_menu_leaks),
                        value = stringResource(
                            if (state.build.leakDetection) {
                                R.string.dev_menu_leaks_on
                            } else {
                                R.string.dev_menu_leaks_off
                            },
                        ),
                    ),
                ),
            )

            AppSectionHeader(title = stringResource(R.string.dev_menu_session))
            AppText(
                // Never the id: an address is what identifies the tester's own account, and the
                // id is the only part that may leave the app.
                text = state.session ?: stringResource(R.string.dev_menu_session_none),
                role = TextRole.BodyLarge,
                modifier = Modifier.testTag("devMenu_sessionValue"),
            )

            AppSectionHeader(title = stringResource(R.string.dev_menu_jumps))
            // Its own column: the outer one spaces sections apart, and rows of a list sit flush.
            Column {
                state.jumps.forEach { jump ->
                    AppListItem(
                        headline = jump.label.resolve(),
                        onClick = { onEvent(DevMenuEvent.JumpClicked(jump)) },
                        modifier = Modifier.testTag("devMenu_${jump.id}Item"),
                    )
                }
            }

            if (state.offlineSupported) {
                AppSectionHeader(title = stringResource(R.string.dev_menu_network))
                AppSwitch(
                    checked = state.offline,
                    onCheckedChange = { onEvent(DevMenuEvent.OfflineToggled(it)) },
                    label = stringResource(R.string.dev_menu_offline),
                    supporting = stringResource(R.string.dev_menu_offline_supporting),
                    modifier = Modifier.testTag("devMenu_offlineSwitch"),
                )
            }

            AppSectionHeader(title = stringResource(R.string.dev_menu_tools))
            AppButton(
                label = stringResource(R.string.dev_menu_notification),
                onClick = { onEvent(DevMenuEvent.NotificationClicked) },
                kind = ButtonKind.Outline,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("devMenu_notificationButton"),
            )
            AppButton(
                label = stringResource(R.string.dev_menu_crash),
                onClick = { onEvent(DevMenuEvent.CrashClicked) },
                kind = ButtonKind.Outline,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("devMenu_crashButton"),
            )
        }
    }
}

@ScreenPreview
@Composable
private fun Preview(
    @PreviewParameter(DevMenuStatePreviews::class) state: DevMenuState,
) = ThemedScreenPreview {
    DevMenuScreen(
        state = state,
    ) {}
}
