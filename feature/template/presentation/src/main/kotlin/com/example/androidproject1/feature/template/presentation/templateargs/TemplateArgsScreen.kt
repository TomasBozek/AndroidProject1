package com.example.androidproject1.feature.template.presentation.templateargs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.feature.template.presentation.R
import com.example.androidproject1.feature.template.presentation.component.TemplateHeadline

@Composable
fun TemplateArgsScreen(
    state: TemplateArgsState,
    onEvent: (TemplateArgsEvent) -> Unit,
) {
    // A screen composes components from :core:ui and nothing else — no Material widget, no colour,
    // no size. AppScaffold is the shell: base surface, system insets, an optional top bar.
    AppScaffold(screenId = "TemplateArgsScreen") {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Tagged so the screen test can assert the route argument actually reached the
            // screen — which is the one thing this variant exists to demonstrate.
            TemplateHeadline(
                text = stringResource(R.string.template_args_title, state.templateId),
                modifier = Modifier.testTag("templateArgs_idValue"),
            )
        }
    }
}

@ScreenPreview
@Composable
private fun Preview(
    @PreviewParameter(TemplateArgsStatePreviews::class) state: TemplateArgsState,
) = ThemedScreenPreview {
    TemplateArgsScreen(
        state = state,
    ) {}
}
