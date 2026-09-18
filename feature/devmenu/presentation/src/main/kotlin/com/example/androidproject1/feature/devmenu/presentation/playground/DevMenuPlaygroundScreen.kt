package com.example.androidproject1.feature.devmenu.presentation.playground

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppCard
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppSectionHeader
import com.example.androidproject1.core.ui.component.AppSelect
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.devmenu.presentation.KnobValues
import com.example.androidproject1.feature.devmenu.presentation.R
import com.example.androidproject1.feature.devmenu.presentation.component.PlaygroundKnob
import com.example.androidproject1.feature.devmenu.presentation.playgroundCatalog
import com.example.androidproject1.feature.devmenu.presentation.playgroundEntry

/**
 * Pick a component, drive its properties from real controls, watch it change — the bench the
 * gallery is not (D77). The screen knows no component: the picker lists the catalog, the stage
 * draws `entry.render(values)`, and the controls under it are one [PlaygroundKnob] per knob.
 * Drawn first: <https://claude.ai/artifact/V4CBL2VxCLW95ZCbJ46vHW>.
 */
@Composable
fun DevMenuPlaygroundScreen(
    state: DevMenuPlaygroundState,
    onEvent: (DevMenuPlaygroundEvent) -> Unit,
) {
    AppScaffold(
        screenId = "DevMenuPlaygroundScreen",
        topBar = {
            AppTopBar(
                title = stringResource(R.string.dev_menu_playground_title),
                onNavigateUp = { onEvent(DevMenuPlaygroundEvent.NavigateUpClicked) },
                navigateUpTestTag = "devMenuPlayground_upButton",
            )
        },
    ) {
        val entry = playgroundEntry(state.entryId) ?: return@AppScaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
        ) {
            AppSelect(
                options = playgroundCatalog.map { it.name },
                selectedIndex = playgroundCatalog.indexOf(entry),
                onSelect = { onEvent(DevMenuPlaygroundEvent.ComponentPicked(playgroundCatalog[it].id)) },
                placeholder = stringResource(R.string.dev_menu_playground_component),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("devMenuPlayground_componentField"),
            )

            // The stage: raised, so the component is seen on the surface a card gives it, and
            // centred, so a small one is not lost in a corner.
            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("devMenuPlayground_stageCard"),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppTheme.spacing.inset.lg),
                    contentAlignment = Alignment.Center,
                ) {
                    entry.render(KnobValues(entry, state.values))
                }
            }

            AppSectionHeader(title = stringResource(R.string.dev_menu_playground_knobs))
            entry.knobs.forEach { knob ->
                PlaygroundKnob(
                    knob = knob,
                    value = state.values[knob.key] ?: knob.default,
                    onChange = { onEvent(DevMenuPlaygroundEvent.KnobChanged(knob.key, it)) },
                )
            }
        }
    }
}

@ScreenPreview
@Composable
private fun Preview(
    @PreviewParameter(DevMenuPlaygroundStatePreviews::class) state: DevMenuPlaygroundState,
) = ThemedScreenPreview {
    DevMenuPlaygroundScreen(
        state = state,
    ) {}
}
