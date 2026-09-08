package com.example.androidproject1.feature.template.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.TextRole

@Composable
fun TemplateScreen(
    state: TemplateState,
    onEvent: (TemplateEvent) -> Unit,
) {
    // A screen composes components from :core:ui and nothing else — no Material widget, no colour,
    // no size. AppScaffold is the shell: base surface, system insets, an optional top bar.
    AppScaffold {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppText(
                text = stringResource(R.string.template_title),
                role = TextRole.Title,
            )
        }
    }
}

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    TemplateScreen(
        state = TemplateState.PREVIEW,
    ) {}
}
